package com.lachesis.appupgradeservice.modules.upgrade.handler;

import com.lachesis.appupgradeservice.modules.upgrade.model.UpgradeRequestBean;
import com.lachesis.appupgradeservice.modules.upgrade.model.UpgradeResponseBean;
import com.lachesis.appupgradeservice.share.NetApiConfig;

import java.util.List;

import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Url;
import rx.Observable;

/**
 * Created by boxue.hao on 2017/9/22.
 */

public interface IHttpUpgradeService {

    @POST("/upgrade/check/{businessId}/mirror")
    @Headers({"Content-Type: application/json","Accept: application/json"})
    public Observable<UpgradeRequestBean> test(@Path("businessId") String businessId, @Body RequestBody requestBody);

    @POST(NetApiConfig.UPDATE_CHECK)
    public Observable<List<UpgradeResponseBean>> checkUpdate(@Url String url, @Body RequestBody requestBody);
}