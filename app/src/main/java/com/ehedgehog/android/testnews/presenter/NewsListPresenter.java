package com.ehedgehog.android.testnews.presenter;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;

import com.ehedgehog.android.testnews.NewsRepository;
import com.ehedgehog.android.testnews.Paginator;
import com.ehedgehog.android.testnews.model.Article;
import com.ehedgehog.android.testnews.network.ApiFactory;
import com.ehedgehog.android.testnews.view.NewsListView;

import java.util.List;

import io.reactivex.disposables.Disposable;

public class NewsListPresenter extends BasePresenter<List<Article>, NewsListView> {

    private Disposable mNewsSubscription;
    private NewsRepository mNewsRepository;
    private Paginator mPaginator;

    private boolean isLoading = false;

    public NewsListPresenter() {
        Log.i("NewsListPresenter", "Presenter creating...");
    }

    @Override
    public void bindView(NewsListView view) {
        super.bindView(view);

        if (mNewsRepository == null)
            mNewsRepository = new NewsRepository(ApiFactory.buildNewsService());

        mNewsRepository.setPaginator(getPaginator());
        mNewsRepository.addListener(new NewsRepository.Listener() {
            @Override
            public void onNewsLoaded(List<Article> articles) {
                setModel(articles);
                getView().updateUI(articles);
            }

            @Override
            public void onLoadingError(String message) {
                getView().logErrorMessage(message);
            }
        });
    }

    @Override
    public void unbindView() {
        super.unbindView();

        if (mNewsSubscription != null && !mNewsSubscription.isDisposed())
            mNewsSubscription.dispose();
    }

    public Paginator getPaginator() {
        if (mPaginator == null) {
            Log.i("NewsListPresenter", "Creating new paginator");
            mPaginator = new Paginator();
        }

        return mPaginator;
    }

    public void loadNews(Context context, String country, String category) {
        setLoading(true);
        mNewsSubscription = mNewsRepository.loadNews(context, country, category);
    }

    public void onScreenScrolledDown(LinearLayoutManager manager) {
        getView().scrollDownList(manager);
    }

    public void onCountrySelected(Context context, String country, String category, int position) {
        getView().changeCountry(country, position);
        Log.i("NewsListPresenter", "Country selected");
        mPaginator.resetCurrentPage();
        loadNews(context, country, category);
    }

    public void onCategorySelected(Context context, String category, String country, int position) {
        getView().changeCategory(category, position);
        Log.i("NewsListPresenter", "Category selected");
        mPaginator.resetCurrentPage();
        loadNews(context, country, category);
    }

    public void onSwipeRefreshing() {
        getView().refreshScreen();
    }

    public void setLoading(boolean isLoading) {
        this.isLoading = isLoading;
    }

    public boolean isLoading() {
        return isLoading;
    }

    public boolean isModelAlreadyLoaded() {
        return mModel != null;
    }
}
