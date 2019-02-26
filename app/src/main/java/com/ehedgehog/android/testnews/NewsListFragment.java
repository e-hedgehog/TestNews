package com.ehedgehog.android.testnews;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.ehedgehog.android.testnews.model.Article;
import com.ehedgehog.android.testnews.model.NewsResult;
import com.ehedgehog.android.testnews.network.ApiFactory;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;
import io.realm.RealmResults;

public class NewsListFragment extends Fragment {

    private static final String TAG = "NewsListFragment";

    private SwipeRefreshLayout mRefreshLayout;
    private RecyclerView mRecyclerView;
    private ProgressBar mProgressBar;

    private List<Article> mArticles;

    private Disposable mNewsSubscription;

    public static NewsListFragment newInstance() {
        return new NewsListFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setRetainInstance(true);

        isOnline();

        if (mArticles == null)
            mArticles = new ArrayList<>();

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_news_list, container, false);

        mRefreshLayout = view.findViewById(R.id.news_list_refresh_container);
        setupSwipeRefresh();

        mProgressBar = view.findViewById(R.id.news_progress_bar);
        mRecyclerView = view.findViewById(R.id.news_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(),
                DividerItemDecoration.VERTICAL));

        loadNews();

        return view;
    }

    @Override
    public void onPause() {
        if (mNewsSubscription != null)
            mNewsSubscription.dispose();

        super.onPause();
    }

    private void loadNews() {
        mNewsSubscription = ApiFactory.buildNewsService()
                .getAllNews("us")
                .map(NewsResult::getArticles)
                .flatMap(articles -> {
                    Realm.init(getActivity());
                    Realm.getDefaultInstance().executeTransaction(realm -> {
                        realm.delete(Article.class);
                        realm.insert(articles);
                    });
                    return Observable.just(articles);
                })
                .onErrorResumeNext(throwable -> {
                    Realm.init(getActivity());
                    Realm realm = Realm.getDefaultInstance();
                    RealmResults<Article> results = realm.where(Article.class).findAll();
                    return Observable.just(realm.copyFromRealm(results));
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::updateUI, throwable ->
                        Log.e(TAG, "Something is wrong", throwable));
    }

    private void updateUI(List<Article> articles) {
        NewsAdapter adapter = (NewsAdapter) mRecyclerView.getAdapter();
        if (adapter == null) {
            adapter = new NewsAdapter(getActivity(), articles);
            mRecyclerView.setAdapter(adapter);
        } else {
            adapter.setArticles(articles);
            adapter.notifyDataSetChanged();
        }

        mProgressBar.setVisibility(View.GONE);
    }

    private boolean isOnline() {
        ConnectivityManager manager = (ConnectivityManager) getActivity()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();

        if (info != null && info.isConnected())
            return true;

        Toast.makeText(getActivity(), "No internet connection", Toast.LENGTH_SHORT).show();
        return false;
    }

    private void setupSwipeRefresh() {
        mRefreshLayout.setColorSchemeResources(
                android.R.color.black,
                android.R.color.holo_blue_light,
                android.R.color.holo_orange_dark
        );
        mRefreshLayout.setOnRefreshListener(() -> {
            mRefreshLayout.setRefreshing(true);
            if (isOnline())
                loadNews();
            mRefreshLayout.setRefreshing(false);
        });
    }
}
