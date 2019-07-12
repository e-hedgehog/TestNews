package com.ehedgehog.android.testnews.di;

import com.ehedgehog.android.testnews.screen.NewsListFragment;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {DataModule.class})
public interface AppComponent {

    void injectNewsListActivity(NewsListFragment fragment);


}
