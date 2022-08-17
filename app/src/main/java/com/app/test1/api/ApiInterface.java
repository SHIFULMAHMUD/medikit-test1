package com.app.test1.api;

import com.app.test1.model.Data;
import com.app.test1.model.ecg.Ecg;
import com.app.test1.model.height.Height;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiInterface {
    @GET("1816678/feeds.json")
    Call<Data> getData(@Query("api_key") String api_key);

    @GET("1816678/feeds.json")
    Call<Ecg> getEcgData(@Query("api_key") String api_key);

    @GET("1826118/feeds.json")
    Call<Height> getHeightData(@Query("api_key") String api_key);
}
