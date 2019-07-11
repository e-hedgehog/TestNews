package com.ehedgehog.android.testnews.view;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.ehedgehog.android.testnews.model.Article;

import java.util.List;

public interface NewsListView {

    void updateUI(List<Article> articles);

    void logErrorMessage(String message);

    void scrollDownList(LinearLayoutManager manager);

    void refreshScreen();

    void changeCountry(String country, int position);

    void changeCategory(String category, int position);

}
