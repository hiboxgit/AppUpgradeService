package com.lachesis.appupgradeservice.modules.upgrade.handler.IRetrofitService;

import com.lachesis.appupgradeservice.modules.upgrade.model.ParamSetInfo;
import com.lachesis.appupgradeservice.modules.upgrade.model.UpgradeResponse;
import com.lachesis.appupgradeservice.share.NetApiConfig;

import java.util.List;

import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import rx.Observable;

/**
 * Created by boxue.hao on 2017/9/22.
 */

public interface IUpdateService {

    @POST(NetApiConfig.UPDATE_CHECK)
//    @Headers({"Content-Type: application/json","Accept: application/json"})
    public Observable<List<UpgradeResponse>> checkUpdate(@Body RequestBody requestBody);

    @GET(NetApiConfig.UPGRADE_INSTALL_DELAY)
    public Observable<ParamSetInfo> getInstallDelayTime();
}
