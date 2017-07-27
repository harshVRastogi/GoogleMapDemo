package com.harsh.mapdemo.loader;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by Harsh Rastogi  on 7/27/2017.
 */

public final class NetworkHelper {
    public static final void loadImage(String url) {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS).build();
        Request request = new Request.Builder()
                .url(url)
                .build();

    }
}
