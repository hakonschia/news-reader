package com.hakon.news_reader;

import com.rometools.rome.feed.synd.SyndEntry;

import java.util.Date;

/**
 * Base class for an RSS or ATOM article
 */
public class NewsArticle {
    /* Atom and RSS */
    private String mTitle;
    private String mURL;

    /* RSS */
    private String mDesc;

    /* Atom */
    private Date mUpdatedDate;


    private static final String TAG = "NewsArticle";


    /**
     * Sets object from a synd entry node
     * @param entry The entry to read from
     */
    public NewsArticle(SyndEntry entry) {
        mTitle = entry.getTitle();

        try {
            mDesc = entry.getDescription().getValue();
        } catch(NullPointerException e) {
            e.printStackTrace();
        }
        mURL = entry.getLink();
        mUpdatedDate = entry.getUpdatedDate();

        // Some feeds have html tags in the descriptions, so remove them
        //mDesc = android.text.Html.fromHtml(mDesc).toString();
    }

    public String getTitle() {
        return mTitle;
    }

    public String getDesc() {
        return mDesc;
    }

    public String getURL() {
        return mURL;
    }

    public Date getUpdatedDate() {
        return mUpdatedDate;
    }

    @Override
    public String toString() {
        return "NewsArticle{" +
                "mTitle='" + mTitle + '\'' +
                ", mURL='" + mURL + '\'' +
                ", mDesc='" + mDesc + '\'' +
                ", mUpdatedDate=" + mUpdatedDate +
                '}';
    }
}