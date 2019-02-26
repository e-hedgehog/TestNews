package com.ehedgehog.android.testnews;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ehedgehog.android.testnews.model.Article;
import com.squareup.picasso.Picasso;

public class NewsHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private ImageView mArticleImage;
    private TextView mTitleTextView;
    private TextView mDescriptionTextView;

    private Article mArticle;

    public NewsHolder(@NonNull View itemView) {
        super(itemView);

        itemView.setOnClickListener(this);

        mArticleImage = itemView.findViewById(R.id.article_item_image);
        mTitleTextView = itemView.findViewById(R.id.article_item_title);
        mDescriptionTextView = itemView.findViewById(R.id.article_item_description);
    }

    public void bind(Article article) {
        mArticle = article;

        mTitleTextView.setText(article.getTitle());
        mDescriptionTextView.setText(article.getDescription());
        Picasso.get().load(article.getUrlToImage()).fit().into(mArticleImage);
    }

    @Override
    public void onClick(View v) {
        Context context = v.getContext();
        Intent i = ArticleActivity.newIntent(context, mArticle);
        context.startActivity(i);
    }
}
