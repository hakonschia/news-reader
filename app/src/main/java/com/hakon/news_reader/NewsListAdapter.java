package com.hakon.news_reader;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class NewsListAdapter extends RecyclerView.Adapter<NewsListAdapter.NewsListViewHolder> {
    private Context mContext;
    private ArrayList<NewsArticle> mArticles;

    private static final String TAG = "NewsListAdapter";

    public static final SimpleDateFormat dateFormater = new SimpleDateFormat("dd-mm-yyyy HH:mm:ss");


    public NewsListAdapter(Context context, ArrayList<NewsArticle> articles) {
        mContext = context;
        mArticles = articles;
    }

    @NonNull
    @Override
    public NewsListAdapter.NewsListViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(
                viewGroup.getContext()).inflate(R.layout.list_newslistitem,
                viewGroup,
                false
        );

        return new NewsListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(NewsListViewHolder viewHolder, final int i) {
        NewsArticle article = mArticles.get(i);
        viewHolder.tvTitle.setText(article.getTitle());
        viewHolder.webDescription.setWebViewClient(new WebViewClient());
        viewHolder.webDescription.loadData(article.getDesc(), "text/html", "UTF-8");

        try {
            viewHolder.tvUpdatedDate.setText(dateFormater.format(article.getUpdatedDate()));
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        viewHolder.parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, ArticleActivity.class);
                intent.putExtra("url", mArticles.get(i).getURL());

                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mArticles.size();
    }


    public static class NewsListViewHolder extends RecyclerView.ViewHolder {
        private RelativeLayout parentLayout;
        private TextView tvTitle;
        private TextView tvUpdatedDate;
        private WebView webDescription;

        public NewsListViewHolder(@NonNull View itemView) {
            super(itemView);

            parentLayout = itemView.findViewById(R.id.list_item_parent_layout);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvUpdatedDate = itemView.findViewById(R.id.tv_updatedDate);
            webDescription = itemView.findViewById(R.id.web_description);
        }
    }
}


