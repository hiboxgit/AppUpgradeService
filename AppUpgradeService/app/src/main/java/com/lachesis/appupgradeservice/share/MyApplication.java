package com.lachesis.appupgradeservice.share;

import android.app.Application;

import com.lachesis.common.CommonLib;
import com.lachesis.common.utils.Utils;

/**
 * 自定义应用程序类
 *
 *（尽量轻量化）
 *
 * Created by Robert on 2017/9/19.
 */

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        init();
    }

    private void init(){
        CommonLib.getInstance().init(this);
        RunDataHelper.getInstance().init();
    }
}
