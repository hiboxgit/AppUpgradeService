package com.lachesis.appupgradeservice.modules.upgrade.handler.IRetrofitService;

import com.lachesis.appupgradeservice.modules.upgrade.model.UpgradeResponse;

import java.util.List;

import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.POST;
import rx.Observable;

/**
 * Created by boxue.hao on 2017/9/22.
 */

public interface IUpdateService {

    @POST("/windranger/ldm/SoftUpgradeManages/getUpgradeSoft")
//    @Headers({"Content-Type: application/json","Accept: application/json"})
    public Observable<List<UpgradeResponse>> checkUpdate(@Body RequestBody requestBody);

}
