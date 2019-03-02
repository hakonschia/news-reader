package com.hakon.news_reader;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.IOException;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;
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
    private Thread articlesUpdater;
    private NewsListAdapter mNewsListAdapter;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.initViews();
        this.updatePreferences();

        final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mRvNewsList.setLayoutManager(layoutManager);

        mArticles = new ArrayList<>();
        mNewsListAdapter = new NewsListAdapter(this, mArticles);
        mRvNewsList.setAdapter(mNewsListAdapter);

        articlesUpdater = new Thread(new Runnable() {
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
        articlesUpdater.start();

        mBtnPreferences.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(MainActivity.this, PreferencesActivity.class), ACTIVITY_REQUEST_ARTICLE);
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
                    articlesUpdater.interrupt();
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

            for(int i = 0; i < Math.min(nodeList.getLength(), mAmountOfArticles); i++) {
                try { // Artificial loading time
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                mArticles.add(new NewsArticle(nodeList.item(i)));

                final int index = i; // Needs to be final to use inside inner class and
                // cant make it final in the loop inializer :))

                // Update the adapter as the articles are loaded
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mNewsListAdapter.notifyItemChanged(index);
                    }
                });
            }
        } catch(XPathExpressionException e) {
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

    /**
     * Downloads a file from the internet asynchronously
     */
    protected static class FileDownloader extends AsyncTask<String, Void, ArrayList<NewsArticle>> {
        @Override
        protected ArrayList<NewsArticle> doInBackground(String... urls) {
            XPath xpath = XPathFactory.newInstance().newXPath();
            final InputSource inputSource = new InputSource(urls[0]);

            try {
                NodeList first = (NodeList)xpath.evaluate("/", inputSource, XPathConstants.NODESET);

                Log.d(TAG, "doInBackground: reading " + urls[0]);
                if(first == null) {
                    Log.d(TAG, "doInBackground: first node is null");
                } else {
                    Log.d(TAG, "doInBackground: " + first.item(0).getNodeName());
                }

                for(int i = 0; i < first.getLength(); i++) {
                    Log.d(TAG, "NodeName: " + first.item(0).getNodeName());
                }

                NodeList nodeList = (NodeList)xpath.evaluate(
                        "//item",  // Find all the item elements in the document
                        inputSource,
                        XPathConstants.NODESET
                );

                ArrayList<NewsArticle> articles = new ArrayList<>();

                for(int i = 0; i < nodeList.getLength(); i++) {
                    articles.add(new NewsArticle(nodeList.item(i)));
                }

                return articles;
            } catch(XPathExpressionException e) {
                e.printStackTrace();
            }

            return null;
        }
    }
}
