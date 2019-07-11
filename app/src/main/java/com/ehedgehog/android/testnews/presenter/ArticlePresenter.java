package com.ehedgehog.android.testnews.presenter;

import com.ehedgehog.android.testnews.model.Article;
import com.ehedgehog.android.testnews.view.ArticleView;

public class ArticlePresenter extends BasePresenter<Article, ArticleView> {

    public void onArticleButtonClicked() {
        getView().openFullArticle(mModel.getUrl());
    }

}
