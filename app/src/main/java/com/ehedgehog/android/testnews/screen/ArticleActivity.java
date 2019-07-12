package com.ehedgehog.android.testnews.screen;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

import com.ehedgehog.android.testnews.model.Article;

public class ArticleActivity extends SingleFragmentActivity {

    private static final String EXTRA_ARTICLE = "com.ehedgehog.android.testnews.article";

    public static Intent newIntent(Context context, Article article) {
        Intent intent = new Intent(context, ArticleActivity.class);
        intent.putExtra(EXTRA_ARTICLE, article);
        return intent;
    }

    @Override
    protected Fragment createFragment() {
        Article article = (Article) getIntent().getSerializableExtra(EXTRA_ARTICLE);
        return ArticleFragment.newInstance(article);
    }
}
