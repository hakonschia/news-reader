package com.hakon.news_reader;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.widget.SwipeRefreshLayout;
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

import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    // TODO: Allow multiple news sources, make mURL into an arraylist (in Preferences, spilt news by "," or something similar)
    /* Primitives / Standard java types */
    private String mURL;
    private Integer mUpdateRate;
    private Integer mAmountOfArticles;
    private ArrayList<NewsArticle> mArticles;
    private Thread mThreadArticlesUpdater;
    private NewsListAdapter mNewsListAdapter;
    private String mFilter;
    private List<SyndEntry> mNewsEntries;

    /* UI elements */
    private Button mBtnPreferences;
    private EditText mEtNewsFilter;
    private RecyclerView mRvNewsList;
    private ProgressBar mPrgLoader;
    private SwipeRefreshLayout mRefreshLayout;

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


    // TODO Rotating the screen shouldn't update the whole thing

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
        mThreadArticlesUpdater = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    Log.d(TAG, "run: fetching");
                    fetchArticles();

                    try {
                        TimeUnit.MINUTES.sleep(mUpdateRate);
                    } catch(InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        mThreadArticlesUpdater.start();


        /* ---------------------------------
            ----- Event listeners -----
        --------------------------------- */

        // TODO: When reaching the bottom of the list: this.updateArticles();

        mBtnPreferences.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(MainActivity.this, PreferencesActivity.class), ACTIVITY_REQUEST_ARTICLE);
            }
        });


        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mThreadArticlesUpdater.interrupt();
                mRefreshLayout.setRefreshing(false); // Removes the refreshing loader icon
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
                // task that is currently the task that will be ran
                timer.cancel();
                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
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

                        // Update the articles and clear
                        updateArticles(true);
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

                    mThreadArticlesUpdater.interrupt();
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
        mRefreshLayout = findViewById(R.id.rfr_swipeLayout);
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

        //mURL = "/home/hakon/Downloads/DDmT8VV4.xml";
        //mURL = "https://www.theregister.co.uk/data_centre/headlines.atom";
        //mURL = "https://nedlasting.geonorge.no/geonorge/Tjenestefeed.xml"; // ATOM example
    }

    /**
     * Fetches the articles and loads the entries into mEntries. Clears the old articles
     */
    private void fetchArticles() {
        this.updatePreferences(); // Make sure the URL is updated

        try {
            // WireFeed feed2 = new WireFeedInput().build(new XmlReader((new URL(mURL))));
            // feed2.getFeedType() = RSS or ATOM
            SyndFeed feed = new SyndFeedInput().build(new XmlReader(new URL(mURL)));
            mNewsEntries = feed.getEntries();

            mArticles.clear(); // Clear articles to make sure the latest ones are added

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mNewsListAdapter.notifyDataSetChanged();
                }
            });

            this.updateArticles(true);
        } catch (FeedException | IOException e) { e.printStackTrace(); }
    }

    /**
     * Updates the articles list
     * @param clear Whether or not to clear the articles
     */
    private void updateArticles(boolean clear) {
        this.updateLoaderVisibility();

        if(clear) {
            mArticles.clear();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mNewsListAdapter.notifyDataSetChanged();
                }
            });
        }

        // Start at the element at position size (which is the next, 0..size-1)
        int from = mArticles.size();

        // Go to whatever is smallest of the amount of entries or the current
        // amount added to the user specified amount to fetch
        int to = Math.min(mNewsEntries.size(), from + mAmountOfArticles);

        this.addArticles(from, to);

        this.updateLoaderVisibility();
    }

    /**
     * Adds articles from the entries to the adapter
     * @param from the starting position of mNewsEntries (inclusive)
     * @param to the ending position of mNewsEntries (non-inclusive)
     */
    private void addArticles(int from, int to) {
        Pattern p = Pattern.compile(mFilter);

        for(int i = from; i < to; i++) {
            try { // Artificial loading time
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            SyndEntry entry = mNewsEntries.get(i);

            if (mFilter.isEmpty()) { // No filter is entered, add all entries
                mArticles.add(new NewsArticle(entry));
            } else { // Search through and only add articles matching the filter
                String title = entry.getTitle();
                String desc = "";

                try {
                    desc = entry.getDescription().getValue();
                } catch(NullPointerException e) {
                    e.printStackTrace();
                }

                Matcher titleMatcher = p.matcher(title);
                Matcher descMatcher = p.matcher(desc);

                if (titleMatcher.find() || descMatcher.find()) {
                    mArticles.add(new NewsArticle(entry));
                }
            }

            // Needs to be redeclared as final to use inside inner class and
            final int index = i;

            // Update the adapter as the articles are loaded
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mNewsListAdapter.notifyItemChanged(index);
                }
            });
        }
    }

    /**
     * Updates the loaders visibility
     */
    private void updateLoaderVisibility() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(mPrgLoader.getVisibility() == View.VISIBLE) {
                    mPrgLoader.setVisibility(View.GONE);
                } else {
                    mPrgLoader.setVisibility(View.VISIBLE);
                }
            }
        });
    }
}