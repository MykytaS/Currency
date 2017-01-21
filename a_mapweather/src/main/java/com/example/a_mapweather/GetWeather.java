package com.example.a_mapweather;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by nikita on 21.01.2017.
 */
public interface GetWeather {


    @GET("/data/2.5/weather?units=metric")
    Call<WeatherCoord> selectByLatLon(@Query("lat") double lat, @Query("lon") double lon, @Query("appid") String appid);

    @GET("/data/2.5/weather?units=metric")
    Call<WeatherCoord> selectByName(@Query("q") String q, @Query("appid") String appid);
}
