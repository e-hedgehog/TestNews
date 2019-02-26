package com.ehedgehog.android.testnews;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ehedgehog.android.testnews.model.Article;

import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<NewsHolder> {

    private Context mContext;
    private List<Article> mArticles;

    public NewsAdapter(Context context, List<Article> articles) {
        mContext = context;
        mArticles = articles;
    }

    @NonNull
    @Override
    public NewsHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.article_item, viewGroup, false);
        return new NewsHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsHolder newsHolder, int i) {
        newsHolder.bind(mArticles.get(i));
    }

    @Override
    public int getItemCount() {
        return mArticles.size();
    }

    public void setArticles(List<Article> articles) {
        mArticles = articles;
    }
}
