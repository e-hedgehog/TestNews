package com.ehedgehog.android.testnews.screen;

import android.content.Context;
import android.util.Log;

import com.ehedgehog.android.testnews.Paginator;
import com.ehedgehog.android.testnews.model.Article;
import com.ehedgehog.android.testnews.model.NewsResult;
import com.ehedgehog.android.testnews.network.NewsService;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;
import io.realm.RealmResults;

public class NewsRepository {

    public interface Listener {
        void onNewsLoaded(List<Article> articles);

        void onLoadingError(String message);
    }

    private Listener mListener;
    private NewsService mService;
    private Paginator mPaginator;

    public NewsRepository(NewsService service) {
        mService = service;
    }

    public void addListener(Listener listener) {
        mListener = listener;
    }

    public Disposable loadNews(Context context, String country, String category) {
        return getAllNews(country, category, mPaginator.getCurrentPage())
                .map(newsResult -> {
                    mPaginator.setupPaginator(newsResult.getTotalResults());
                    return newsResult.getArticles();
                })
                .flatMap(articles -> {
                    Realm.init(context);
                    Realm.getDefaultInstance().executeTransaction(realm -> {
                        if (mPaginator.isFirst())
                            realm.delete(Article.class);
                        realm.insert(articles);
                    });
                    return Observable.just(articles);
                })
                .onErrorResumeNext(throwable -> {
                    Realm.init(context);
                    Realm realm = Realm.getDefaultInstance();
                    RealmResults<Article> results = realm.where(Article.class).findAll();
                    return Observable.just(realm.copyFromRealm(results));
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mListener::onNewsLoaded, throwable ->
                        mListener.onLoadingError(throwable.getMessage()));
    }

    private Observable<NewsResult> getAllNews(String country, String category, int page) {
        Log.i("NewsRepository", "Loading " + page + " page");
        return mService.getAllNews(country, category, page);
    }

    public Paginator getPaginator() {
        return mPaginator;
    }

    public void setPaginator(Paginator paginator) {
        mPaginator = paginator;
    }
}
