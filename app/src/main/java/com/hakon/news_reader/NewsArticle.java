package com.hakon.news_reader;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

public class NewsArticle {
    private String mTitle;
    private String mDesc;
    private String mURL;

    private static final String TAG = "NewsArticle";

    public NewsArticle(String title, String desc, String URL) {
        this.mTitle = title;
        this.mDesc = desc;
        this.mURL = URL;
    }

    /**
     * Sets object from a DOM node
     * @param node The node to read from
     */
    public NewsArticle(Node node) {
        XPath xpath = XPathFactory.newInstance().newXPath();
        NodeList children = node.getChildNodes();

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

    @Override
    public String toString() {
        return "NewsArticle{" +
                "mTitle='" + mTitle + '\'' +
                ", mDesc='" + mDesc + '\'' +
                ", mLink='" + mURL + '\'' +
                '}';
    }
}