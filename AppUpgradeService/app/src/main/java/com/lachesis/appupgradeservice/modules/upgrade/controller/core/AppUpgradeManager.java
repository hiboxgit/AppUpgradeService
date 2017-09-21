package com.lachesis.appupgradeservice.modules.upgrade.controller.core;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.lachesis.appupgradeservice.modules.upgrade.controller.broadcast.InstallEventReceiver;
import com.lachesis.appupgradeservice.share.RunDataHelper;
import com.lachesis.common.CommonLib;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

/**
 * app升级管理类
 *
 * Created by Robert on 2017/9/19.
 */

public class AppUpgradeManager {

    private static String TAG = "AppUpgradeManager";

    private static AppUpgradeManager instance = new AppUpgradeManager();

    private String apkPath = "";
    private boolean installResult;
    private UpgradeCallBack callBack;

    private InstallEventReceiver installEventReceiver;


    public AppUpgradeManager() {

        installEventReceiver = new InstallEventReceiver();
    }

    public static AppUpgradeManager getInstance() {
        return instance;
    }

    public void upgrade(UpgradeCallBack callBack) {

        this.callBack = callBack;
        upgrade();
    }

    public void upgrade() {

        checkUpdate();
        download();
        install();
    }

    public void checkUpdate(){

    }

    public void download(){


    }

    public void install(){
        registerApkInstallReceiver(CommonLib.getInstance().getContext());
        doIntallApk();
    }

    private void doIntallApk() {
        Log.i(TAG, "开始进行apk安装...");
        try {
            // 申请su权限
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream dataOutputStream = new DataOutputStream(process.getOutputStream());
            // 执行pm install命令
            String command = "pm install -r " + apkPath + "\n";
            dataOutputStream.write(command.getBytes(Charset.forName("utf-8")));
            dataOutputStream.flush();
            dataOutputStream.writeBytes("exit\n");
            dataOutputStream.flush();
            process.waitFor();
            BufferedReader errorStream = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String msg = "";
            String line;
            // 读取命令的执行结果
            while ((line = errorStream.readLine()) != null) {
                msg += line;
            }
            Log.d(TAG, "install msg is " + msg);
            // 如果执行结果中包含Failure字样就认为是安装失败，否则就认为安装成功
            if (!msg.contains("Failure")) {
                installResult = true;
            } else {
                installResult = false;
            }

        } catch (Exception e) {
            Log.e(TAG, "doIntallApk" + e.toString());
        }

        Log.i(TAG, "apk install complete, result is : " + (installResult ? "Success" : "Failed"));

    }

    public void doAfterInstalled(String packageName) {
        packageName = packageName.replace("package:", "");

        Log.i(TAG, packageName + "has been installed!");

        if (callBack != null) {
            callBack.onComplete();
        } else {

        }

        //取消安装监听器
        unRegisterApkInstallReceiver(CommonLib.getInstance().getContext());

    }

    private void registerApkInstallReceiver(Context context) {
        Log.i(TAG, "动态注册APK安装监听器");
        try {
            IntentFilter installIntentFilter = new IntentFilter();
            installIntentFilter.addDataScheme("package");
            installIntentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
            installIntentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
            installIntentFilter.addAction(Intent.ACTION_PACKAGE_REPLACED);
            context.registerReceiver(installEventReceiver, installIntentFilter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void unRegisterApkInstallReceiver(Context context) {
        Log.i(TAG, "取消APK安装监听器");
        try {
            context.unregisterReceiver(installEventReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static interface UpgradeCallBack {

        public void onComplete();

        public void onError();
    }
}
