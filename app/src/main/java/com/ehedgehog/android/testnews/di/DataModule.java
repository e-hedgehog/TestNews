package com.ehedgehog.android.testnews.di;

import android.support.annotation.NonNull;

import com.ehedgehog.android.testnews.BuildConfig;
import com.ehedgehog.android.testnews.screen.NewsRepository;
import com.ehedgehog.android.testnews.Paginator;
import com.ehedgehog.android.testnews.network.ApiKeyInterceptor;
import com.ehedgehog.android.testnews.network.NewsService;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
public class DataModule {

    @Provides
    @Singleton
    NewsRepository provideNewsRepository(@NonNull NewsService service) {
        return new NewsRepository(service);
    }

    @Provides
    @Singleton
    NewsService provideNewsService(@NonNull Retrofit retrofit) {
        return retrofit.create(NewsService.class);
    }

    @Provides
    @Singleton
    Retrofit provideRetrofit(@NonNull OkHttpClient client) {
        return new Retrofit.Builder()
                .baseUrl(BuildConfig.API_ENDPOINT)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
    }

    @Provides
    @Singleton
    OkHttpClient provideOkHttpClient() {
        return new OkHttpClient.Builder()
                .addInterceptor(new HttpLoggingInterceptor())
                .addInterceptor(new ApiKeyInterceptor())
                .build();
    }

    @Provides
    @Singleton
    Paginator providePaginator() {
        return new Paginator();
    }

}
