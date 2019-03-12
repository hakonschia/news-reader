package com.hakon.news_reader;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
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
import android.widget.Toast;

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
    /* Standard types */
    private String mFilter;         // What to filter the articles on
    private String mURL;            // The URL to fetch articles from
    private Integer mUpdateRate;    // How often the articles should auto-update
    private Integer mAmountOfArticles;          // How many articles to fetch at a time
    private ArrayList<NewsArticle> mArticles;   // The articles
    private Thread mThreadArticlesUpdater;      // The thread that fetches the articles
    private NewsListAdapter mNewsListAdapter;   // Adapter for the RecyclerView
    private FeedFetcher feedFetcher;            // Fetches and updates articles
    private int mOldArticlesAmount;             // Used for when the filter is changed, to keep the same amount

    /* UI elements */
    private Button mBtnPreferences;
    private EditText mEtNewsFilter;
    private RecyclerView mRvNewsList;
    private ProgressBar mPrgLoader;
    private SwipeRefreshLayout mRefreshLayout;

    /* Private constants */
    private static final String TAG = "MainActivity";

    /* Public constants */
    public static final String PREFS_SETTINGS = "preferences";

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
        mOldArticlesAmount = 0;
        mArticles = new ArrayList<>();
        mNewsListAdapter = new NewsListAdapter(this, mArticles);
        mRvNewsList.setAdapter(mNewsListAdapter);
        feedFetcher = new FeedFetcher(mURL);


        // Create the thread responsible for updating the articles
        mThreadArticlesUpdater = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    updatePreferences(); // Make sure the everything is up to date

                    feedFetcher.fetchArticles(); // Fetch the latest articles

                    updateArticles(0, mAmountOfArticles);
                    mOldArticlesAmount = mArticles.size();

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

                        // Just update articles, don't fetch new ones
                        // Get the amount of articles that was there before a filter was entered
                        updateArticles(0, mOldArticlesAmount);
                    }
                }, 750);
            }
        });

        mRvNewsList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            private boolean updating = false;

            // Idk if the updating variable can cause problems if it's on multiple threads
            // so I'm pretty sure adding synchronized makes sure it wont :)
            @Override
            public synchronized void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                // -1 = scroll up, 1 = scroll down
                // It's extremely sensitive, I'll tell you that
                if(!recyclerView.canScrollVertically(1)) { // Can't scroll down, load more articles
                    if(!updating) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                if(!updating) {
                                    updating = true;

                                    int from = mArticles.size();

                                    // Go to whatever is smallest of the amount of entries or the current
                                    // amount added to the user specified amount to fetch
                                    int to = Math.min(feedFetcher.getFeedSize(), from + mAmountOfArticles);

                                    updateArticles(from, to);

                                    updating = false;
                                }
                            }
                        }).start();
                    }
                }
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
        SharedPreferences preferences = getSharedPreferences(PREFS_SETTINGS, 0);

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

        //mURL = "https://folk.ntnu.no/haakosc/feed.rss";
        mURL = "/home/hakon/Downloads/feed.rss";
    }

    /**
     * Helper function that updates the RecyclerView dataset and updates the loader
     * @param from Where the start in the entries
     * @param to Where the end in the entries
     */
    private void updateArticles(int from, int to) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mPrgLoader.setVisibility(View.VISIBLE);
            }
        });

        feedFetcher.getArticles(mArticles, mFilter, from, to);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mNewsListAdapter.notifyDataSetChanged();
                mPrgLoader.setVisibility(View.GONE);
            }
        });
    }
}