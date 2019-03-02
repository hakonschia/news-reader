package com.hakon.news_reader;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndEntry;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Date;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

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
    private Date mUpdateTime;


    private static final String TAG = "NewsArticle";


    /**
     * Sets object from a synd entry node
     * @param entry The entry to read from
     */
    public NewsArticle(SyndEntry entry) {
        mTitle = entry.getTitle();
        mDesc = entry.getDescription().getValue();
        mURL = entry.getLink();
        mUpdateTime = entry.getUpdatedDate();

        // None of these work for the atom feed so imma kms xDDD
        Log.d(TAG, "NewsArticle: link: " + entry.getLink() + ", uri: " + entry.getUri());
        /*
        try {
            Node title = (Node)xpath.evaluate("title", node, XPathConstants.NODE);
            mTitle = title.getTextContent();
        } catch (XPathExpressionException e) {
            Log.d(TAG, "NewsArticle: Error reading title");
        }

        try {
            Node desc = (Node)xpath.evaluate("description", node, XPathConstants.NODE);
            mDesc = desc.getTextContent();
        } catch (XPathExpressionException e) {
            Log.d(TAG, "NewsArticle: Error reading title");
        }

        try {
            Node link = (Node)xpath.evaluate("link", node, XPathConstants.NODE);
            mURL = link.getTextContent();
        } catch (XPathExpressionException e) {
            Log.d(TAG, "NewsArticle: Error reading title");
        }
        */
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
}