package com.lachesis.appupgradeservice.modules.upgrade.handler;

import com.google.gson.Gson;
import com.lachesis.appupgradeservice.modules.upgrade.model.UpgradeResponseBean;
import com.lachesis.appupgradeservice.share.NetApiConfig;
import com.lachesis.common.network.RetrofitManager;

import org.json.JSONObject;

import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import rx.Observable;

/**
 * Created by boxue.hao on 2017/9/22.
 */

public class UpgradeHttpHandler {

    public static Observable<List<UpgradeResponseBean>> checkApkUpdate(List<UpgradeResponseBean> updateList) {
        Gson gson = new Gson();
        String jsonStr = gson.toJson(updateList);

        RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonStr);
        return RetrofitManager.getInstance().getRetrofit(NetApiConfig.SERVER_HOST).create(IUpgradeApi.class)
                .checkUpdate(body);
    }
}
