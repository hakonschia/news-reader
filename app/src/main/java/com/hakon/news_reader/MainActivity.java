package com.hakon.news_reader;

import android.app.Activity;
import android.content.Context;
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
import android.widget.Toast;

import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

public class MainActivity extends AppCompatActivity {

    // TODO: Allow multiple news sources, make mURL into an arraylist (in Preferences, spilt news by "," or something similar)
    /* Primitives / Standard java types */
    String mURL;
    Integer mUpdateRate;
    Integer mAmountOfArticles;
    ArrayList<NewsArticle> mArticles;

    /* UI elements */
    private Button mBtnPreferences;
    private EditText mEtNewsFilter;
    private RecyclerView mRvNewsList;

    /* Private constants */
    private static final String TAG = "MainActivity";

    /* Public constants */
    public static final String PREFS_NAME = "preferences";
    public static final String DEFUALT_URL = "https://www.cisco.com/c/dam/global/no_no/about/rss.xml";
    public static final int DEFAULT_ARTICLES_AMOUNT = 20;
    public static final int DEFAULT_UPDATE_RATE = 20;

    public static final String PREFS_URL = "url";
    public static final String PREFS_AMOUNT_OF_ARTICLES = "articlesAmount";
    public static final String PREFS_UPDATE_RATE = "updateRate";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.initViews();
        this.updatePreferences();

        mBtnPreferences.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, PreferencesActivity.class));
            }
        });

        final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mRvNewsList.setLayoutManager(layoutManager);

        this.updateArticles();
    }


    /**
     * Initializes all UI elements
     */
    private void initViews() {
        mBtnPreferences = findViewById(R.id.btn_preferences);
        mEtNewsFilter = findViewById(R.id.et_newsFilter);
        mRvNewsList = findViewById(R.id.rv_newsList);
    }

    /**
     * Updates preferences
     */
    private void updatePreferences() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, 0);

        mURL = preferences.getString(
                PREFS_URL,
                DEFUALT_URL
        );
        mAmountOfArticles = preferences.getInt(
                PREFS_AMOUNT_OF_ARTICLES,
                DEFAULT_ARTICLES_AMOUNT
        );
        mUpdateRate = preferences.getInt(
                PREFS_UPDATE_RATE,
                DEFAULT_UPDATE_RATE
        );
    }

    /**
     * Updates mArticles and sets the adapter
     */
    private void updateArticles() {
        this.updatePreferences();

        try { // Get the news items and set the adapter
            mArticles = new FileDownloader().execute(mURL).get();

            if(mArticles != null) {
                mRvNewsList.setAdapter(new NewsListAdapter(this, mArticles));
            }
        } catch(ExecutionException e) {
            e.printStackTrace();
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
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
                NodeList nodeList = (NodeList)xpath.evaluate(
                        "//item",
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
