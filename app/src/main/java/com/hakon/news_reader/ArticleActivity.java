package com.hakon.news_reader;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;

public class ArticleActivity extends AppCompatActivity {
    private WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);

        Bundle data = getIntent().getExtras();

        mWebView = findViewById(R.id.webView);
        mWebView.loadUrl(data.getString("url"));
    }

    /*
    @Override
    public void onBackPressed() {
        if(mWebView.canGoBack()) { // If the webview can go back, do so, or else
            mWebView.goBack();     // return to MainActivity
        } else {
            finish();
        }
    }
    */
}
