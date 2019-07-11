package com.ehedgehog.android.testnews;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.ehedgehog.android.testnews.model.Article;
import com.ehedgehog.android.testnews.presenter.ArticlePresenter;
import com.ehedgehog.android.testnews.view.ArticleView;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ArticleFragment extends Fragment implements ArticleView {

    private static final String TAG = "ArticleFragment";
    private static final String ARG_ARTICLE = "article";

    private ImageView mArticleImage;
    private TextView mTitleTextView;
    private TextView mAuthorTextView;
    private TextView mPublishedTextView;
    private TextView mContentTextView;
    private Button mArticleButton;

    private Article mArticle;

    private ArticlePresenter mPresenter;

    public static ArticleFragment newInstance(Article article) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_ARTICLE, article);

        ArticleFragment fragment = new ArticleFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        if (getArguments() != null)
            mArticle = (Article) getArguments().getSerializable(ARG_ARTICLE);

        mPresenter = new ArticlePresenter();
        mPresenter.bindView(this);
        mPresenter.setModel(mArticle);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_article, container, false);

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            activity.getSupportActionBar().setSubtitle(mArticle.getSource().getName());
        }

        mArticleImage = view.findViewById(R.id.article_image);
        Picasso.get().load(mArticle.getUrlToImage()).into(mArticleImage);

        mTitleTextView = view.findViewById(R.id.article_title);
        mTitleTextView.setText(mArticle.getTitle());

        mAuthorTextView = view.findViewById(R.id.article_author);
        mAuthorTextView.setText(mArticle.getAuthor());

        mPublishedTextView = view.findViewById(R.id.article_published_at);
        mPublishedTextView.setText(formatDate(mArticle.getPublishedAt()));

        mContentTextView = view.findViewById(R.id.article_content);
        mContentTextView.setText(mArticle.getContent());

        mArticleButton = view.findViewById(R.id.article_button);
        mArticleButton.setOnClickListener(v -> mPresenter.onArticleButtonClicked());

        return view;
    }

    private String formatDate(String date) {
        DateFormat format = new SimpleDateFormat(
                getResources().getString(R.string.api_date_format), Locale.getDefault());
        Date apiDate = null;
        try {
            apiDate = format.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        format = new SimpleDateFormat(
                getResources().getString(R.string.date_format), Locale.getDefault());

        return format.format(apiDate);
    }

    @Override
    public void openFullArticle(String url) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }
}
