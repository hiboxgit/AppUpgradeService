package com.dudu.bluetooth;

import android.app.Application;
import android.os.Build;

import com.fota.iport.info.MobileParamInfo;

/**
 * Created by raise.yang on 16/11/03.
 */
public class BTApplication extends Application {

    public static boolean is_test = false;

    @Override
    public void onCreate() {
        super.onCreate();
        MobileParamInfo.initInfo(this, Build.SERIAL, Constant.version, Constant.oem, Constant.models, Constant.token, Constant.platform, Constant.deviceType);
    }
}
