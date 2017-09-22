package com.lachesis.common.network;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.RequestBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by boxue.hao on 2017/9/22.
 */

public class RetrofitManager {

    private static RetrofitManager instance = new RetrofitManager();
    private Retrofit retrofit;

    private boolean sShowDebugLog = true;

    public static RetrofitManager getInstance(){
        return instance;
    }

    private RetrofitManager() {

    }

    public Retrofit getRetrofit(String url){

        Retrofit.Builder builder = new Retrofit.Builder()
                .client(getOkHttpClient())
                .baseUrl(url)   // 设置服务端地址
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())   // 设置支持RX数据响应
                .addConverterFactory(GsonConverterFactory.create()); // 使用Gson封装、解析数据

        retrofit = builder.build();

        return retrofit;
    }

    private OkHttpClient getOkHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .connectTimeout(15, TimeUnit.SECONDS);

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor(
                new HttpLoggingInterceptor.Logger() {
                    @Override
                    public void log(String message) {
                        blockLog("OkHttp", message);
                    }
                });
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        builder.addInterceptor(logging);

        return builder.build();
    }

    private void blockLog(String tag, String msg) {
        if (sShowDebugLog) {
            if (TextUtils.isEmpty(msg)) {
                return;
            }

            String[] lines = msg.split(System.getProperty("line.separator"));
            if (msg.startsWith("-->") && !msg.startsWith("--> END")) {
                Log.d(tag, "┌───────────────────────────────────────────────────────────────────────────────────────");
            }
            for (String line : lines) {
                Log.d(tag, "│" + " " + line);
            }
            if (msg.startsWith("<-- END") || msg.startsWith("<-- HTTP FAILED")) {
                Log.d(tag, "└───────────────────────────────────────────────────────────────────────────────────────");
            }
        }
    }

    public void test(){
        JSONObject result = new JSONObject();
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), result.toString());
    }
}
