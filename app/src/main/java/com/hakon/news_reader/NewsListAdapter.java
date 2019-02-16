package com.hakon.news_reader;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

public class NewsListAdapter extends RecyclerView.Adapter<NewsListAdapter.NewsListViewHolder> {
    private Context mContext;
    private ArrayList<String> mTextData;


    public NewsListAdapter(Context context, ArrayList<String> textData) {
        mContext = context;
        mTextData = textData;
    }

    @NonNull
    @Override
    public NewsListAdapter.NewsListViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(
                viewGroup.getContext()).inflate(R.layout.list_newslistitem,
                viewGroup,
                false
        );

        NewsListViewHolder viewHolder = new NewsListViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(NewsListViewHolder viewHolder, int i) {
        viewHolder.tvTitle.setText(mTextData.get(i));

        viewHolder.parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(
                        mContext,
                        "apetor",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mTextData.size();
    }


    public static class NewsListViewHolder extends RecyclerView.ViewHolder {
        private RelativeLayout parentLayout;
        private TextView tvTitle;

        public NewsListViewHolder(@NonNull View itemView) {
            super(itemView);

            parentLayout = itemView.findViewById(R.id.parent_layout);
            tvTitle = itemView.findViewById(R.id.tv_title);
        }
    }
}


