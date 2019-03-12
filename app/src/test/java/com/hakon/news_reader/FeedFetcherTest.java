package com.hakon.news_reader;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class FeedFetcherTest {
    static FeedFetcher feedFetcher;
    static ArrayList<NewsArticle> articles;


    @BeforeClass
    public static void init() {
        feedFetcher = new FeedFetcher("https://folk.ntnu.no/haakosc/feed.rss"); // Because yes
        articles = new ArrayList<>();
    }

    @Test
    public void testGetArticles() {
        feedFetcher.fetchArticles();
        feedFetcher.getArticles(articles, "", 0, 3);

        NewsArticle first = articles.get(0);
        assertEquals(
                "Yes but are they queuing for Office?",
                first.getTitle()
        );

        assertEquals(
                "https://www.reddit.com/r/GlobalOffensive/comments/azpyhd/yes_but_are_they_queuing_for_office/",
                first.getURL()
        );


        NewsArticle third = articles.get(2);
        assertEquals(
                "kennyS achieves his highest HLTV rating ever vs Revolution in a 16-0 (2.91 Rating)",
                third.getTitle()
        );

        assertEquals(
                "https://www.reddit.com/r/GlobalOffensive/comments/azu61m/kennys_achieves_his_highest_hltv_rating_ever_vs/",
                third.getURL()
        );
    }

    @Test
    public void testFilterArticles() {
        feedFetcher.getArticles(articles, "kennyS", 0, 3);

        assertEquals(articles.size(), 1);

        NewsArticle first = articles.get(0);
        assertEquals(
                "kennyS achieves his highest HLTV rating ever vs Revolution in a 16-0 (2.91 Rating)",
                first.getTitle()
        );

        // Change filter, see that it gives a different
        feedFetcher.getArticles(articles, "Dennis", 0, 3);

        assertEquals(articles.size(), 1);

        first = articles.get(0);
        assertEquals(
                "https://www.reddit.com/r/GlobalOffensive/comments/azrlr8/nip_on_twitter_dennis_denniscsgod_edman_takes_a/",
                first.getURL()
        );
    }
}

/* First 3 entries:
Title 1: Yes but are they queuing for Office?
Title 3: kennyS achieves his higest HLTV rating ever vs Revolution in a 16-0 (2.91 Rating)

<entry>
    <author>
        <name>/u/corey_cobra_kid</name>
        <uri>https://www.reddit.com/user/corey_cobra_kid</uri>
    </author>
    <category term="GlobalOffensive" label="r/GlobalOffensive"/>
    <content type="html">&lt;table&gt; &lt;tr&gt;&lt;td&gt; &lt;a href=&quot;https://www.reddit.com/r/GlobalOffensive/comments/azpyhd/yes_but_are_they_queuing_for_office/&quot;&gt; &lt;img src=&quot;https://a.thumbs.redditmedia.com/QXr6MtbiZdHg-d1DrBL4C6GS_A5FxUz86TOpjLu95w4.jpg&quot; alt=&quot;Yes but are they queuing for Office?&quot; title=&quot;Yes but are they queuing for Office?&quot; /&gt; &lt;/a&gt; &lt;/td&gt;&lt;td&gt; &amp;#32; submitted by &amp;#32; &lt;a href=&quot;https://www.reddit.com/user/corey_cobra_kid&quot;&gt; /u/corey_cobra_kid &lt;/a&gt; &lt;br/&gt; &lt;span&gt;&lt;a href=&quot;https://i.redd.it/6fqzvcpzydl21.jpg&quot;&gt;[link]&lt;/a&gt;&lt;/span&gt; &amp;#32; &lt;span&gt;&lt;a href=&quot;https://www.reddit.com/r/GlobalOffensive/comments/azpyhd/yes_but_are_they_queuing_for_office/&quot;&gt;[comments]&lt;/a&gt;&lt;/span&gt; &lt;/td&gt;&lt;/tr&gt;&lt;/table&gt;</content>
    <id>t3_azpyhd</id>
    <link href="https://www.reddit.com/r/GlobalOffensive/comments/azpyhd/yes_but_are_they_queuing_for_office/" />
    <updated>2019-03-11T06:21:29+00:00</updated>
    <title>Yes but are they queuing for Office?</title>
</entry>

<entry>
    <author>
        <name>/u/Sindrelele</name>
        <uri>https://www.reddit.com/user/Sindrelele</uri>
    </author>
    <category term="GlobalOffensive" label="r/GlobalOffensive"/>
    <content type="html">&lt;table&gt; &lt;tr&gt;&lt;td&gt; &lt;a href=&quot;https://www.reddit.com/r/GlobalOffensive/comments/azrlr8/nip_on_twitter_dennis_denniscsgod_edman_takes_a/&quot;&gt; &lt;img src=&quot;https://b.thumbs.redditmedia.com/6GUT4V3618nLxdRQKPzwj2755gB2aFS14SmLDzqhkwY.jpg&quot; alt=&quot;Nip on twitter : «Dennis &amp;quot;@denniscsgod&amp;quot; Edman takes a break from competitive play due to fatigue. William &amp;quot;@drakenCSGO&amp;quot; Sundin temporarily steps in for upcoming tournaments.»&quot; title=&quot;Nip on twitter : «Dennis &amp;quot;@denniscsgod&amp;quot; Edman takes a break from competitive play due to fatigue. William &amp;quot;@drakenCSGO&amp;quot; Sundin temporarily steps in for upcoming tournaments.»&quot; /&gt; &lt;/a&gt; &lt;/td&gt;&lt;td&gt; &amp;#32; submitted by &amp;#32; &lt;a href=&quot;https://www.reddit.com/user/Sindrelele&quot;&gt; /u/Sindrelele &lt;/a&gt; &lt;br/&gt; &lt;span&gt;&lt;a href=&quot;https://twitter.com/nipgaming/status/1105045271522742273?s=21&quot;&gt;[link]&lt;/a&gt;&lt;/span&gt; &amp;#32; &lt;span&gt;&lt;a href=&quot;https://www.reddit.com/r/GlobalOffensive/comments/azrlr8/nip_on_twitter_dennis_denniscsgod_edman_takes_a/&quot;&gt;[comments]&lt;/a&gt;&lt;/span&gt; &lt;/td&gt;&lt;/tr&gt;&lt;/table&gt;</content>
    <id>t3_azrlr8</id>
    <link href="https://www.reddit.com/r/GlobalOffensive/comments/azrlr8/nip_on_twitter_dennis_denniscsgod_edman_takes_a/" />
    <updated>2019-03-11T10:00:01+00:00</updated>
    <title>Nip on twitter : «Dennis &quot;@denniscsgod&quot; Edman takes a break from competitive play due to fatigue. William &quot;@drakenCSGO&quot; Sundin temporarily steps in for upcoming tournaments.»</title>
</entry>

<entry>
    <author>
        <name>/u/kane91123</name>
        <uri>https://www.reddit.com/user/kane91123</uri>
    </author>
    <category term="GlobalOffensive" label="r/GlobalOffensive"/>
    <content type="html">&lt;table&gt; &lt;tr&gt;&lt;td&gt; &lt;a href=&quot;https://www.reddit.com/r/GlobalOffensive/comments/azu61m/kennys_achieves_his_highest_hltv_rating_ever_vs/&quot;&gt; &lt;img src=&quot;https://b.thumbs.redditmedia.com/yfv7lmim8DgAF-ZWXFDiYLodjg13IRoPunTepv8IUJg.jpg&quot; alt=&quot;kennyS achieves his highest HLTV rating ever vs Revolution in a 16-0 (2.91 Rating)&quot; title=&quot;kennyS achieves his highest HLTV rating ever vs Revolution in a 16-0 (2.91 Rating)&quot; /&gt; &lt;/a&gt; &lt;/td&gt;&lt;td&gt; &amp;#32; submitted by &amp;#32; &lt;a href=&quot;https://www.reddit.com/user/kane91123&quot;&gt; /u/kane91123 &lt;/a&gt; &lt;br/&gt; &lt;span&gt;&lt;a href=&quot;https://www.hltv.org/stats/matches/mapstatsid/82553/g2-vs-revolution?contextIds=7167&amp;amp;contextTypes=player&quot;&gt;[link]&lt;/a&gt;&lt;/span&gt; &amp;#32; &lt;span&gt;&lt;a href=&quot;https://www.reddit.com/r/GlobalOffensive/comments/azu61m/kennys_achieves_his_highest_hltv_rating_ever_vs/&quot;&gt;[comments]&lt;/a&gt;&lt;/span&gt; &lt;/td&gt;&lt;/tr&gt;&lt;/table&gt;</content>
    <id>t3_azu61m</id>
    <link href="https://www.reddit.com/r/GlobalOffensive/comments/azu61m/kennys_achieves_his_highest_hltv_rating_ever_vs/" />
    <updated>2019-03-11T14:36:45+00:00</updated>
    <title>kennyS achieves his highest HLTV rating ever vs Revolution in a 16-0 (2.91 Rating)</title>
</entry>
 */