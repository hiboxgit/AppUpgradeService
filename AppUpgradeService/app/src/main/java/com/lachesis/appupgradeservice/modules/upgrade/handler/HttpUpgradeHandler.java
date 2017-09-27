package com.lachesis.appupgradeservice.modules.upgrade.handler;

import com.google.gson.Gson;
import com.lachesis.appupgradeservice.modules.upgrade.model.UpgradeRequestBean;
import com.lachesis.appupgradeservice.modules.upgrade.model.UpgradeResponseBean;
import com.lachesis.appupgradeservice.share.NetApiConfig;
import com.lachesis.appupgradeservice.share.RunDataHelper;
import com.lachesis.common.network.RetrofitManager;

import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import rx.Observable;

/**
 * Created by boxue.hao on 2017/9/22.
 */

public class HttpUpgradeHandler {

    public static Observable<List<UpgradeResponseBean>> checkApkUpdate(List<UpgradeRequestBean> updateList) {
        Gson gson = new Gson();
        String jsonStr = gson.toJson(updateList);
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonStr);
        return getUpgradeService()
                .checkUpdate(body);
    }


    public static IUpgradeApi getUpgradeService(){
        return RetrofitManager.getInstance().getRetrofit(RunDataHelper.getInstance().getServerHostConfig()).create(IUpgradeApi.class);
    }
}
