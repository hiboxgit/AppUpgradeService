package com.lachesis.appupgradeservice.modules.upgrade.controller.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.lachesis.appupgradeservice.modules.upgrade.controller.core.AppUpgradeManager;

/**
 * apk安装事件广播接收器
 * <p>
 * Created by Robert on 2017/9/19.
 */
public class InstallEventReceiver extends BroadcastReceiver {

    private static String TAG = "InstallEventReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        String packageName = intent.getDataString();

        if (Intent.ACTION_PACKAGE_ADDED.equals(action)) {

            Log.i(TAG, packageName + "安装成功");

            AppUpgradeManager.getInstance().doAfterInstalled(packageName);
        } else if (Intent.ACTION_PACKAGE_REMOVED.equals(action)) {


            Log.i(TAG, packageName + "删除成功");

        } else if (Intent.ACTION_PACKAGE_REPLACED.equals(action)) {

            Log.i(TAG, packageName + "替换成功");

            AppUpgradeManager.getInstance().doAfterInstalled(packageName);
        }
    }
}
