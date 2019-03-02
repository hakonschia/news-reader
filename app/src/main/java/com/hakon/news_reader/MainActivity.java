package com.hakon.news_reader;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.rometools.rome.feed.WireFeed;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.WireFeedInput;
import com.rometools.rome.io.XmlReader;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

public class MainActivity extends AppCompatActivity {

    // TODO: Allow multiple news sources, make mURL into an arraylist (in Preferences, spilt news by "," or something similar)
    /* Primitives / Standard java types */
    private String mURL;
    private Integer mUpdateRate;
    private Integer mAmountOfArticles;
    private ArrayList<NewsArticle> mArticles;
    private Thread mArticlesUpdater;
    private NewsListAdapter mNewsListAdapter;
    private String mFilter;

    /* UI elements */
    private Button mBtnPreferences;
    private EditText mEtNewsFilter;
    private RecyclerView mRvNewsList;
    private ProgressBar mPrgLoader;

    /* Private constants */
    private static final String TAG = "MainActivity";

    /* Public constants */
    public static final String PREFS_NAME = "preferences";
    public static final String DEFAULT_URL = "https://www.cisco.com/c/dam/global/no_no/about/rss.xml";
    public static final int DEFAULT_ARTICLES_AMOUNT = 20;
    public static final int DEFAULT_UPDATE_RATE = 20;

    public static final String PREFS_URL = "url";
    public static final String PREFS_AMOUNT_OF_ARTICLES = "articlesAmount";
    public static final String PREFS_UPDATE_RATE = "updateRate";

    public static final int ACTIVITY_REQUEST_ARTICLE = 1;


    // TODO Rotating the screen shouldnt update the whole thing

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.initViews();
        this.updatePreferences();

        final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mRvNewsList.setLayoutManager(layoutManager);

        mFilter = "";
        mArticles = new ArrayList<>();
        mNewsListAdapter = new NewsListAdapter(this, mArticles);
        mRvNewsList.setAdapter(mNewsListAdapter);

        // Create the thread responsible for updating the articles
        mArticlesUpdater = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    updateArticles();

                    try {
                        TimeUnit.MINUTES.sleep(mUpdateRate);
                    } catch(InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        mArticlesUpdater.start();


        /* ---------------------------------
            ----- Event listeners -----
        --------------------------------- */

        mBtnPreferences.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(MainActivity.this, PreferencesActivity.class), ACTIVITY_REQUEST_ARTICLE);
            }
        });

        mEtNewsFilter.addTextChangedListener(new TextWatcher() {
            private Timer timer = new Timer();

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mFilter = s.toString();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            /**
             * Update the list 1 second after the last character was typed
             */
            @Override
            public void afterTextChanged(Editable s) {
                // Cancel the old task first, then create a new
                // task that is currently the task that will ba ran
                timer.cancel();
                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        mArticlesUpdater.interrupt(); // Send an interrupt to the sleeping thread

                        // Close the keyboard
                        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                        try {
                            imm.hideSoftInputFromWindow(
                                    getCurrentFocus().getWindowToken(),
                                    0
                            );
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                        }
                    }
                }, 1000);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == MainActivity.ACTIVITY_REQUEST_ARTICLE) {
            if(resultCode == RESULT_OK) {
                if(data.getBooleanExtra("updated", false)) {
                    // Some preferences were updated, interrupt the
                    // sleeping thread to make it update
                    mArticlesUpdater.interrupt();
                }
            }
        }
    }

    /**
     * Initializes all UI elements
     */
    private void initViews() {
        mBtnPreferences = findViewById(R.id.btn_preferences);
        mEtNewsFilter = findViewById(R.id.et_newsFilter);
        mRvNewsList = findViewById(R.id.rv_newsList);
        mPrgLoader = findViewById(R.id.prg_loader);
    }

    /**
     * Updates preferences
     */
    private void updatePreferences() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, 0);

        mURL = preferences.getString(
                PREFS_URL,
                DEFAULT_URL
        );
        mAmountOfArticles = preferences.getInt(
                PREFS_AMOUNT_OF_ARTICLES,
                DEFAULT_ARTICLES_AMOUNT
        );
        mUpdateRate = preferences.getInt(
                PREFS_UPDATE_RATE,
                DEFAULT_UPDATE_RATE
        );

        mURL = "https://www.theregister.co.uk/data_centre/headlines.atom";
        //mURL = "https://nedlasting.geonorge.no/geonorge/Tjenestefeed.xml"; // ATOM example
    }

    /**
     * Updates mArticles and sets the adapter
     */
    private void updateArticles() {
        this.updateLoaderVisibility();
        this.updatePreferences();

        XPath xpath = XPathFactory.newInstance().newXPath();
        InputSource inputSource = new InputSource(mURL);

        try {
            // ATOM feeds fuck up on namespaces and this is the only way i figured out so far
            NodeList first = (NodeList)xpath.evaluate("/*[local-name()='feed']", inputSource, XPathConstants.NODESET);

            NodeList nodeList = (NodeList)xpath.evaluate(
                    "//item",  // Find all the item elements in the document
                    inputSource,
                    XPathConstants.NODESET
            );

            mArticles.clear(); // TODO make it just add more articles instead of clearing all

            // In case there were items in the recycler view already, the adapter
            // needs to be notified that the data set was changed
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mNewsListAdapter.notifyDataSetChanged();
                }
            });

            SyndFeed feed = new SyndFeedInput().build(new XmlReader(new URL(mURL)));
            WireFeed feed2 = new WireFeedInput().build(new XmlReader((new URL(mURL))));

            // TODO: Dont fetch the article itself all the time, just on the update or something, or force fetch by the user
            // TODO: Only get articles from the filter

            // RSS or ATOM can be found from getFeedType()
            Log.d(TAG, "updateArticles: feed type: " + feed2.getFeedType());

            for (SyndEntry entry : feed.getEntries()) {
                Log.d(TAG, "updateArticles: " + entry.getTitle());
                NewsArticle n = new NewsArticle(entry);
                mArticles.add(n);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mNewsListAdapter.notifyItemChanged(mArticles.size() - 1);
                    }
                });
            }

            List<SyndEntry> entries = feed.getEntries();

            mArticles.clear(); // TODO make it just add more articles instead of clearing all

            Log.d(TAG, "updateArticles: " + Math.min(entries.size(), mAmountOfArticles));
            for(int i = 0; i < Math.min(entries.size(), mAmountOfArticles); i++) {
                try { // Artificial loading time
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                mArticles.add(new NewsArticle(entries.get(i)));

                final int index = i; // Needs to be final to use inside inner class and
                // cant make it final in the loop initializer :))

                // Update the adapter as the articles are loaded
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mNewsListAdapter.notifyItemChanged(index);
                    }
                });
            }
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        } catch (FeedException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.updateLoaderVisibility();
    }

    /**
     * Updates the loaders visibility
     */
    private void updateLoaderVisibility() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                // TODO; find a way to oneline swap it (setVisibility.(!visible))
                if(mPrgLoader.getVisibility() == View.VISIBLE) {
                    mPrgLoader.setVisibility(View.GONE);
                } else {
                    mPrgLoader.setVisibility(View.VISIBLE);
                }
            }
        });
    }
}