package com.lachesis.appupgradeservice.modules.upgrade.controller.core;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.lachesis.appupgradeservice.modules.upgrade.controller.broadcast.InstallEventReceiver;
import com.lachesis.appupgradeservice.modules.upgrade.handler.HttpUpgradeHandler;
import com.lachesis.appupgradeservice.modules.upgrade.model.UpgradeRequestBean;
import com.lachesis.appupgradeservice.modules.upgrade.model.UpgradeResponseBean;
import com.lachesis.appupgradeservice.share.RunDataHelper;
import com.lachesis.common.CommonLib;
import com.lachesis.common.network.HttpTool;
import com.lachesis.common.utils.AppUtils;
import com.lachesis.common.utils.SDCardUtils;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadListener;

import org.greenrobot.eventbus.EventBus;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.internal.util.ActionSubscriber;
import rx.schedulers.Schedulers;

/**
 * app升级管理类
 *
 * Created by Robert on 2017/9/19.
 */

public class AppUpgradeManager {

    private static final String TAG = "AppUpgradeManager";

    private static AppUpgradeManager instance = new AppUpgradeManager();

    private static final String APK_PREFIX = "com.lachesis";
    private String apkPath = "";
    private boolean installResult;
    private UpgradeCallBack callBack;
    private InstallEventReceiver installEventReceiver;

    private Subscription  startTaskSubscription;
    private Subscription  checkUpdateSubscription;

    private List<UpgradeResponseBean> upgradeResponseList;
    private int downloadApkCount;

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
//        Observable.timer(0, TimeUnit.SECONDS)
        startTaskSubscription = Observable.just(1)
                .subscribeOn(Schedulers.newThread())
                .subscribe(result -> {
                    checkUpdate();
                });
    }

    public void checkUpdate(){

        upgradeResponseList = null;
        List<UpgradeRequestBean> requestData = getApkVersionInfo();
        checkUpdateSubscription = HttpUpgradeHandler.checkApkUpdate(requestData)
                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    upgradeResponseList = result;
                    download(result);
                }, throwable -> {

                });
    }

    public void download(List<UpgradeResponseBean> upgradeInfoList){

        HashMap<String, String> downloadInfoList = new HashMap<String, String>();

        for(int i=0; i<upgradeInfoList.size(); i++){
            String apkPath = SDCardUtils.getSDCardPath()+upgradeInfoList.get(i).getSoftName()+".apk";
            downloadInfoList.put(upgradeInfoList.get(i).getSoftPath(),apkPath);
        }

        //下载队列任务开始
        downloadApkCount = 0;
        HttpTool.downloadFiles(downloadInfoList, new FileDownloadListener() {
            @Override
            protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {

            }

            @Override
            protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {

            }

            @Override
            protected void completed(BaseDownloadTask task) {
                Log.i(TAG,"task complete :"+task.getTag());
                doOnEachDownloadTaskComplete();
            }

            @Override
            protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {

            }

            @Override
            protected void error(BaseDownloadTask task, Throwable e) {
                doOnEachDownloadTaskComplete();
            }

            @Override
            protected void warn(BaseDownloadTask task) {

            }
        });
    }
    private void doOnEachDownloadTaskComplete(){
        downloadApkCount++;
        if(upgradeResponseList.size() == downloadApkCount){
            //全部下载完毕
            doOnCompleteDownload();
        }
    }

    private void doOnCompleteDownload(){
//        EventBus.getDefault().post(new );
    }
    public void install(){
        registerApkInstallReceiver(CommonLib.getInstance().getContext());
        doIntallApk();
    }

    private List<UpgradeRequestBean> getApkVersionInfo(){
        UpgradeRequestBean item;
        List<UpgradeRequestBean> softwareVersionInfoList = new ArrayList<>();
        softwareVersionInfoList.clear();

        //获取联新已经安装的APK列表
        List<AppUtils.AppInfo> installedApkList = AppUtils.getAppsInfo(APK_PREFIX);

        for(int i=0;i<installedApkList.size();i++){ //获取apk版本信息
            item = new UpgradeRequestBean();
            item.setSoftMark(installedApkList.get(i).getPackageName());
            item.setSoftVersion(installedApkList.get(i).getVersionCode());
            softwareVersionInfoList.add(item);
        }
        return softwareVersionInfoList;
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
