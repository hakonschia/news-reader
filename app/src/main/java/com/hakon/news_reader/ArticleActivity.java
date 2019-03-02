package com.hakon.news_reader;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

public class ArticleActivity extends AppCompatActivity {
    private WebView mWebView;
    private ProgressBar mPrgLoader;

    private static final String TAG = "ArticleActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);

        Bundle data = getIntent().getExtras();

        mWebView = findViewById(R.id.webView);
        mPrgLoader = findViewById(R.id.prg_loaderArticleActivity);

        this.updateLoaderVisibility();
        mWebView.setWebViewClient(new WebViewClient() {
            // when clicking some places on the webpages the loader appears but never goes away
            // probably because onPageFinished isn't always called or something
            // https://stackoverflow.com/questions/3149216/how-to-listen-for-a-webview-finishing-loading-a-url)
            public void onPageFinished(WebView view, String url) {
                updateLoaderVisibility();
            }
        });
        mWebView.loadUrl(data.getString("url"));
    }


    @Override
    public void onBackPressed() {
        if(mWebView.canGoBack()) { // If the webview can go back, do so, or else
            mWebView.goBack();     // return to MainActivity
        } else {
            finish();
        }
    }

    /**
     * Updates the loaders visibility
     */
    private void updateLoaderVisibility() {

        // TODO; find a way to oneline swap it (setVisibility.(!visible))
        if(mPrgLoader.getVisibility() == View.VISIBLE) {
            mPrgLoader.setVisibility(View.GONE);
        } else {
            mPrgLoader.setVisibility(View.VISIBLE);
        }
    }
}
