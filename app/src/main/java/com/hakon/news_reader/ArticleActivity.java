package com.hakon.news_reader;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

        mPrgLoader.setVisibility(View.VISIBLE);
        mWebView.setWebViewClient(new WebViewClient() {
            public void onPageFinished(WebView view, String url) {
                mPrgLoader.setVisibility(View.GONE);
            }
        });
        mWebView.loadUrl(data.getString("url"));
    }

    @Override
    public void onBackPressed() {
        if(mWebView.canGoBack()) { // If the WebView can go back, do so, or else
            mWebView.goBack();     // return to MainActivity
        } else {
            finish();
        }
    }
}
