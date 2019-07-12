package com.ehedgehog.android.testnews;

import android.app.Application;
import android.support.annotation.NonNull;

import com.ehedgehog.android.testnews.di.AppComponent;
import com.ehedgehog.android.testnews.di.DaggerAppComponent;
import com.ehedgehog.android.testnews.di.DataModule;

public class AppDelegate extends Application {

    private static AppComponent sAppComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        sAppComponent = DaggerAppComponent.builder()
                .dataModule(new DataModule())
                .build();
    }

    @NonNull
    public static AppComponent getAppComponent() {
        return sAppComponent;
    }
}
