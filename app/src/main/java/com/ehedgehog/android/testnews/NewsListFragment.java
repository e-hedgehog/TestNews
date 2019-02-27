package com.ehedgehog.android.testnews;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.ehedgehog.android.testnews.model.Article;
import com.ehedgehog.android.testnews.model.NewsResult;
import com.ehedgehog.android.testnews.network.ApiFactory;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;
import io.realm.RealmResults;

public class NewsListFragment extends Fragment {

    private static final String TAG = "NewsListFragment";

    private static final int ITEMS_PER_PAGE = 20;

    private SwipeRefreshLayout mRefreshLayout;
    private RecyclerView mRecyclerView;
    private ProgressBar mProgressBar;
    private Spinner mCategorySpinner;
    private Spinner mCountrySpinner;

    private int mCurrentPage;
    private String mCategory;
    private String mCountry;

    private int mTotalItems;
    private int mPagesCount;
    private boolean isLoading = false;

    private Disposable mNewsSubscription;

    public static NewsListFragment newInstance() {
        return new NewsListFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        isOnline();

        mCurrentPage = 1;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_news_list, container, false);

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            activity.getSupportActionBar().setIcon(R.mipmap.ic_launcher);
        }

        mRefreshLayout = view.findViewById(R.id.news_list_refresh_container);
        setupSwipeRefresh();

        mCategorySpinner = view.findViewById(R.id.news_category_spinner);
        setupCategorySpinner();

        mCountrySpinner = view.findViewById(R.id.news_country_spinner);
        setupCountrySpinner();

        mProgressBar = view.findViewById(R.id.news_progress_bar);
        mRecyclerView = view.findViewById(R.id.news_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(),
                DividerItemDecoration.VERTICAL));
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int visibleItemsCount = layoutManager.getChildCount();
                int invisibleItemsCount = layoutManager.findFirstVisibleItemPosition();
                int totalItemsCount = layoutManager.getItemCount();
                if ((visibleItemsCount + invisibleItemsCount) >= totalItemsCount) {
                    if ((mCurrentPage <= mPagesCount) && !isLoading) {
                        Log.i(TAG, "Loading new data...");
                        mCurrentPage++;
                        mProgressBar.setVisibility(View.VISIBLE);
                        loadNews(getAllNews(mCountry, mCategory, mCurrentPage));
                    }
                }
            }
        });


        loadNews(getAllNews(mCountry, mCategory, mCurrentPage));

        return view;
    }

    @Override
    public void onPause() {
        if (mNewsSubscription != null)
            mNewsSubscription.dispose();

        super.onPause();
    }

    private void loadNews(Observable<NewsResult> observable) {
        mNewsSubscription = observable
                .map(newsResult -> {
                    isLoading = true;
                    mTotalItems = newsResult.getTotalResults();
                    mPagesCount = (int) Math.ceil(mTotalItems / ITEMS_PER_PAGE);
                    return newsResult.getArticles();
                })
                .flatMap(articles -> {
                    Realm.init(getActivity());
                    Realm.getDefaultInstance().executeTransaction(realm -> {
                        if (mCurrentPage == 1)
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

    private Observable<NewsResult> getAllNews(String country, String category, int page) {
        return ApiFactory.buildNewsService()
                .getAllNews(country, category, page);
    }

    private Observable<NewsResult> searchNews(String query, int page) {
        return ApiFactory.buildNewsService()
                .searchNews(query, page);
    }

    private void updateUI(List<Article> articles) {
        NewsAdapter adapter = (NewsAdapter) mRecyclerView.getAdapter();
        if (adapter == null) {
            adapter = new NewsAdapter(getActivity(), articles);
            mRecyclerView.setAdapter(adapter);
        } else {
            if (mCurrentPage == 1) {
                adapter.setArticles(articles);
                adapter.notifyDataSetChanged();
            } else {
                adapter.addAll(articles);
                adapter.notifyItemRangeInserted(
                        mCurrentPage * ITEMS_PER_PAGE, ITEMS_PER_PAGE);
            }
        }

        isLoading = false;
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
            mCurrentPage = 1;
            if (isOnline()) {
                loadNews(getAllNews(mCountry, mCategory, mCurrentPage));
            }
            mRefreshLayout.setRefreshing(false);
        });
    }

    private void setupCategorySpinner() {
        ArrayAdapter adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.categories_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mCategorySpinner.setAdapter(adapter);

        String[] categories = getActivity()
                .getResources().getStringArray(R.array.categories_array);
        mCategory = categories[NewsPreferences.getStoredCategory(getActivity())];

        mCategorySpinner.setSelection(NewsPreferences.getStoredCategory(getActivity()));
        mCategorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mCategory = categories[position];
                NewsPreferences.setStoredCategory(getActivity(), position);
                mProgressBar.setVisibility(View.VISIBLE);
                mCurrentPage = 1;
                loadNews(getAllNews(mCountry, mCategory, mCurrentPage));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void setupCountrySpinner() {
        ArrayAdapter adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.countries_array_names, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mCountrySpinner.setAdapter(adapter);

        String[] countries = getActivity()
                .getResources().getStringArray(R.array.countries_array);
        mCountry = countries[NewsPreferences.getStoredCountry(getActivity())];

        mCountrySpinner.setSelection(NewsPreferences.getStoredCountry(getActivity()));
        mCountrySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mCountry = countries[position];
                NewsPreferences.setStoredCountry(getActivity(), position);
                mProgressBar.setVisibility(View.VISIBLE);
                mCurrentPage = 1;
                loadNews(getAllNews(mCountry, mCategory, mCurrentPage));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
}
