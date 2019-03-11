package com.hakon.news_reader;

import android.util.Log;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FeedFetcher {
    private String mURL;
    private List<SyndEntry> mEntries;

    private static final String TAG = "FeedFetcher";


    /**
     * Creates a new object.
     * @param url The url to fetch from
     */
    public FeedFetcher(String url) {
        mURL = url;
    }


    /**
     * Reads the URL and fetches the articles from the feed
     */
    public void fetchArticles() {
        try {
            SyndFeed feed = new SyndFeedInput().build(new XmlReader(new URL(mURL)));
            mEntries = feed.getEntries();
        } catch (FeedException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets the amount of articles in the feed
     * @return The amount of articles in the feed
     */
    public int getFeedSize() {
        return mEntries.size();
    }

    /**
     * Creates a list of articles from the current entries
     * @param articles The list to add the articles to. If from is 0, this is cleared first.
     * @param filter What to filter the articles on
     * @param from Where the start in the entries
     * @param to Where to end in the entries
     */
    public void getArticles(ArrayList<NewsArticle> articles, String filter, int from, int to) {
        Pattern p = Pattern.compile(filter);

        if(from == 0) { // Starting from the top, clear the current articles
            articles.clear();
        }

        to = Math.min(to, mEntries.size());

        for(int i = from; i < to; i++) {
            try { // Artificial loading time
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            SyndEntry entry = mEntries.get(i);

            if (filter.isEmpty()) { // No filter is entered, add all entries
                articles.add(new NewsArticle(entry));
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
                    articles.add(new NewsArticle(entry));
                }
            }
        }
    }
}
