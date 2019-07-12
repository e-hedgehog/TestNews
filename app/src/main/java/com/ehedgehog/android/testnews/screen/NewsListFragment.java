package com.ehedgehog.android.testnews.screen;

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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.ehedgehog.android.testnews.AppDelegate;
import com.ehedgehog.android.testnews.NewsPreferences;
import com.ehedgehog.android.testnews.Paginator;
import com.ehedgehog.android.testnews.R;
import com.ehedgehog.android.testnews.model.Article;
import com.ehedgehog.android.testnews.presenter.NewsListPresenter;
import com.ehedgehog.android.testnews.view.NewsListView;

import java.util.List;

import javax.inject.Inject;

public class NewsListFragment extends Fragment implements NewsListView {

    private static final String TAG = "NewsListFragment";

    private SwipeRefreshLayout mRefreshLayout;
    private RecyclerView mRecyclerView;
    private ProgressBar mProgressBar;
    private Spinner mCategorySpinner;
    private Spinner mCountrySpinner;

    private String mCategory;
    private String mCountry;

    private NewsListPresenter mPresenter;
    @Inject
    Paginator mPaginator;
    @Inject
    NewsRepository mRepository;

    public static NewsListFragment newInstance() {
        return new NewsListFragment();
    }

    @Override
    public void onAttach(Context context) {
        AppDelegate.getAppComponent().injectNewsListActivity(this);
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        isOnline();

//        if (savedInstanceState == null) {
//            Log.i(TAG, "New presenter");
        mPresenter = new NewsListPresenter(mRepository, mPaginator, this, getActivity());
        mPresenter.setupRepository();
//        } else {
//            Log.i(TAG, "Restore presenter");
//            mPresenter = PresenterManager.get().restorePresenter(savedInstanceState);
//        }

//        mPaginator = mPresenter.getPaginator();
//        mPaginator.resetCurrentPage();
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
                mPresenter.onScreenScrolledDown(layoutManager);
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!mPresenter.isModelAlreadyLoaded() && !mPresenter.isLoading()) {
            Log.i(TAG, "loading in onResume");
            mPresenter.loadNews(mCountry, mCategory);
        }
    }

    @Override
    public void onPause() {
        mPresenter.unbindView();

        super.onPause();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
//        Log.i(TAG, "Saving presenter");
//        PresenterManager.get().savePresenter(mPresenter, outState);
    }

    @Override
    public void updateUI(List<Article> articles) {
        NewsAdapter adapter = (NewsAdapter) mRecyclerView.getAdapter();
        if (adapter == null) {
            adapter = new NewsAdapter(getActivity(), articles);
            mRecyclerView.setAdapter(adapter);
        } else {
            if (mPaginator.isFirst()) {
                adapter.setArticles(articles);
                adapter.notifyDataSetChanged();
            } else {
                adapter.addAll(articles);
                adapter.notifyItemRangeInserted(mPaginator.getCurrentPage() *
                        Paginator.ITEMS_PER_PAGE, Paginator.ITEMS_PER_PAGE);
            }
        }

        mPresenter.setLoading(false);
        mProgressBar.setVisibility(View.GONE);
    }

    @Override
    public void logErrorMessage(String message) {
        Log.e(TAG, message);
    }

    @Override
    public void scrollDownList(LinearLayoutManager manager) {
        int visibleItemsCount = manager.getChildCount();
        int invisibleItemsCount = manager.findFirstVisibleItemPosition();
        int totalItemsCount = manager.getItemCount();
        if ((visibleItemsCount + invisibleItemsCount) >= totalItemsCount) {
            if ((mPaginator.getCurrentPage() <= mPaginator.getPagesCount())
                    && !mPresenter.isLoading()) {
                mPaginator.incrementCurrentPage();
                Log.i(TAG, "Loading new data... " + mPaginator.getCurrentPage() + " page");
                mProgressBar.setVisibility(View.VISIBLE);
                mPresenter.loadNews(mCountry, mCategory);
            }
        }
    }

    @Override
    public void refreshScreen() {
        mRefreshLayout.setRefreshing(true);
        mPaginator.resetCurrentPage();
        if (isOnline()) {
            Log.i(TAG, "Refreshing");
            mPresenter.loadNews(mCountry, mCategory);
        }
        mRefreshLayout.setRefreshing(false);
    }

    @Override
    public void changeCountry(String country, int position) {
        mCountry = country;
        NewsPreferences.setStoredCountry(getActivity(), position);
        mProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void changeCategory(String category, int position) {
        mCategory = category;
        NewsPreferences.setStoredCategory(getActivity(), position);
        mProgressBar.setVisibility(View.VISIBLE);
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
        mRefreshLayout.setOnRefreshListener(() ->
                mPresenter.onSwipeRefreshing());
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
                mPresenter.onCategorySelected(categories[position], mCountry, position);
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
                mPresenter.onCountrySelected(countries[position], mCategory, position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
}
