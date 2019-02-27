package com.ehedgehog.android.testnews.network;

import com.ehedgehog.android.testnews.model.NewsResult;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface NewsService {

    @GET("v2/top-headlines")
    Observable<NewsResult> getAllNews(@Query("country") String country, @Query("page") int page);

}
