package com.ehedgehog.android.testnews;

import android.os.Bundle;

import com.ehedgehog.android.testnews.presenter.BasePresenter;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class PresenterManager {

    private static final String KEY_PRESENTER_ID = "presenterId";

    private static PresenterManager sManager;

    private final AtomicLong mCurrentId;

    private final Cache<Long, BasePresenter<?, ?>> mCache;

    public PresenterManager(long maxSize, long expirationValue, TimeUnit expirationUnit) {
        mCurrentId = new AtomicLong();

        mCache = CacheBuilder.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expirationValue, expirationUnit)
                .build();
    }

    public static PresenterManager get() {
        if (sManager == null)
            sManager = new PresenterManager(10, 30, TimeUnit.SECONDS);

        return sManager;
    }

    public <P extends BasePresenter<?, ?>> P restorePresenter(Bundle savedInstanceState) {
        Long presenterId = savedInstanceState.getLong(KEY_PRESENTER_ID);
        P presenter = (P) mCache.getIfPresent(presenterId);
        mCache.invalidate(presenterId);
        return presenter;
    }

    public void savePresenter(BasePresenter<?, ?> presenter, Bundle outState) {
        long presenterId = mCurrentId.incrementAndGet();
        mCache.put(presenterId, presenter);
        outState.putLong(KEY_PRESENTER_ID, presenterId);
    }
}
