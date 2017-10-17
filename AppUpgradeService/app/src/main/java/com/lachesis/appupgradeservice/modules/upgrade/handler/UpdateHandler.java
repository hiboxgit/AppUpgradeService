package com.lachesis.appupgradeservice.modules.upgrade.handler;

import com.google.gson.Gson;
import com.lachesis.appupgradeservice.modules.upgrade.handler.IRetrofitService.IUpdateService;
import com.lachesis.appupgradeservice.modules.upgrade.model.ParamSetInfo;
import com.lachesis.appupgradeservice.modules.upgrade.model.UpgradeRequest;
import com.lachesis.appupgradeservice.modules.upgrade.model.UpgradeResponse;
import com.lachesis.appupgradeservice.share.RunDataHelper;
import com.lachesis.common.network.RetrofitManager;

import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import rx.Observable;

/**
 * Created by boxue.hao on 2017/9/22.
 */

public class UpdateHandler {

    public static Observable<List<UpgradeResponse>> checkApkUpdate(List<UpgradeRequest> updateList) {
        Gson gson = new Gson();
        String jsonStr = gson.toJson(updateList);
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonStr);
        return getUpgradeService()
                .checkUpdate(body);
    }

    public static Observable<ParamSetInfo> getUpgradeDelayTime() {
        return getUpgradeService()
                .getInstallDelayTime();
    }

    public static IUpdateService getUpgradeService() {
        return RetrofitManager.getInstance().getRetrofit(RunDataHelper.getInstance().getServerHostConfig()).create(IUpdateService.class);
    }
}
