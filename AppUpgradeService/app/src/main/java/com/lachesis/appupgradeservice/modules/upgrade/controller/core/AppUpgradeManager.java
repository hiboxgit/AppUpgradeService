package com.lachesis.appupgradeservice.modules.upgrade.controller.core;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.lachesis.appupgradeservice.modules.upgrade.controller.broadcast.InstallEventReceiver;
import com.lachesis.appupgradeservice.modules.upgrade.handler.HttpUpgradeHandler;
import com.lachesis.appupgradeservice.modules.upgrade.model.UpgradeCompleteEvent;
import com.lachesis.appupgradeservice.modules.upgrade.model.UpgradeConfigInfo;
import com.lachesis.appupgradeservice.modules.upgrade.model.UpgradePackageInfo;
import com.lachesis.appupgradeservice.modules.upgrade.model.UpgradeRequestBean;
import com.lachesis.appupgradeservice.modules.upgrade.model.UpgradeResponseBean;
import com.lachesis.appupgradeservice.modules.upgrade.model.UpgradeTipEvent;
import com.lachesis.appupgradeservice.share.NetApiConfig;
import com.lachesis.common.CommonLib;
import com.lachesis.common.network.HttpTool;
import com.lachesis.common.network.bean.DownloaderConfigBean;
import com.lachesis.common.utils.AppUtils;
import com.lachesis.common.utils.FileUtils;
import com.lachesis.common.utils.SDCardUtils;
import com.lachesis.common.utils.ShellUtils;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadListener;
import com.liulishuo.filedownloader.FileDownloader;

import org.greenrobot.eventbus.EventBus;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * app升级管理类
 * <p>
 * Created by Robert on 2017/9/19.
 */

public class AppUpgradeManager {

    private static final String TAG = "AppUpgradeManager";

    private static AppUpgradeManager instance = new AppUpgradeManager();

    private static final String APK_PREFIX = "com.lachesis";
    //    private String apkPath = "";
    private String updateNote = "升级内容";
    private boolean installResult;
    private UpgradeCallBack callBack;
    private InstallEventReceiver installEventReceiver;

    private Subscription startTaskSubscription;
    private Subscription checkUpdateSubscription;
    private Subscription installTaskSubscription;
    private Subscription delayShowTipSubscription;
    private Subscription delay2DoSubscription;

    List<UpgradePackageInfo> upgradePackageInfoList = new ArrayList<>();
    private List<UpgradeResponseBean> upgradeResponseList;
    private List<UpgradeConfigInfo> wait2InstallList;
    private int downloadApkCount;
    private int installedApkCount;

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
        Log.i(TAG, "开始升级...");
//        Observable.timer(0, TimeUnit.SECONDS)
        startTaskSubscription = Observable.just(1)
                .subscribeOn(Schedulers.newThread())
                .subscribe(result -> {
                    checkUpdate();
                });
    }

    public void checkUpdate() {

        Log.i(TAG, "开始检查更新...");
        upgradeResponseList = null;
        List<UpgradeRequestBean> requestData = getApkVersionInfo();
        checkUpdateSubscription = HttpUpgradeHandler.checkApkUpdate(requestData)
                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    Log.i(TAG, "接收到升级响应");

                    if (result == null || result.size() == 0) {


                    } else {
                        upgradeResponseList = new ArrayList<UpgradeResponseBean>();
                        upgradeResponseList.clear();
                        for (int i = 0; i < result.size(); i++) {
                            if (result.get(i).getSoftPath() != null && !result.get(i).getSoftPath().equals("")) {
                                upgradeResponseList.add(result.get(i));
                            }
                        }
                        Log.i(TAG, "接收到的有效升级地址列表个数为：" + upgradeResponseList.size());
                        if (upgradeResponseList.size() > 0) {
                            download(upgradeResponseList);
                        } else {
                            Log.i(TAG, "无可用升级包！");
                        }

                    }

                }, throwable -> {

                });
    }

    public void download(List<UpgradeResponseBean> upgradeInfoList) {
        Log.i(TAG, "开始下载...:" + upgradeInfoList.size());
        upgradePackageInfoList.clear();
        wait2InstallList = new ArrayList<UpgradeConfigInfo>();
        wait2InstallList.clear();

        Log.i(TAG,"准备循环...");
        for (int i = 0; i < upgradeInfoList.size(); i++) {
            Log.i(TAG,"循环更新待升级包..."+i);
            String softMark = upgradeInfoList.get(i).getSoftMark();
//            String[] nameList = softMark.split("."); //有问题，执行不下去
//            String apkName = nameList[nameList.length - 1];
            String apkName = softMark;//upgradeInfoList.get(i).getSoftName();

            String localPath = SDCardUtils.getSDCardPath() +"ApkFiles/"+ apkName + ".apk";
            String remoteUrl = NetApiConfig.SERVER_HOST.substring(0, NetApiConfig.SERVER_HOST.length() - 1) + upgradeInfoList.get(i).getSoftPath();
            upgradePackageInfoList.add(new UpgradePackageInfo(softMark, remoteUrl, localPath));

            Log.i(TAG, "添加到待升级列表->localPath:" + localPath + ",remoteUrl:" + remoteUrl);

            //
            wait2InstallList.add(new UpgradeConfigInfo(localPath));
        }

        if (wait2InstallList.size() <= 0) {
            Log.i(TAG, "没有下载任务可用...");
            return;
        }
        Log.i(TAG, "开始多任务下载...");
        //下载队列任务开始
        downloadApkCount = 0;
        List<DownloaderConfigBean> downloaderConfigList = new ArrayList<>();
        for (int i = 0; i < upgradePackageInfoList.size(); i++) {
            UpgradePackageInfo item = upgradePackageInfoList.get(i);

            DownloaderConfigBean config = new DownloaderConfigBean(item.getRemoteUrl(),
                    item.getLocalPath(),
                    item.getSoftMark());

            downloaderConfigList.add(config);
        }
        HttpTool.downloadFiles(downloaderConfigList, new FileDownloadListener() {
            @Override
            protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {

            }

            @Override
            protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {

            }

            @Override
            protected void blockComplete(BaseDownloadTask task) {
                Log.i(TAG, "blockComplete ");
            }

            @Override
            protected void completed(BaseDownloadTask task) {
                Log.i(TAG, "升级包" + task.getTag() + "下载完成 ");
                doOnEachDownloadTaskComplete();
            }

            @Override
            protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {

            }

            @Override
            protected void error(BaseDownloadTask task, Throwable e) {
                Log.i(TAG, "升级包" + task.getTag() + "下载失败");
                doOnEachDownloadTaskComplete();
            }

            @Override
            protected void warn(BaseDownloadTask task) {

            }
        });
    }

    private void doOnEachDownloadTaskComplete() {
        downloadApkCount++;
        if (upgradeResponseList.size() == downloadApkCount) {
            //全部下载完毕
            doOnCompleteDownload();
        }
    }

    private void doOnCompleteDownload() {
        Log.i(TAG, "全部下载完成！");
        EventBus.getDefault().post(new UpgradeTipEvent(updateNote));
//        registerApkInstallReceiver(CommonLib.getInstance().getContext());
//        installedApkCount = 0;
//        doIntallApk("/mnt/internal_sd/test.apk");
    }

    public void install() {
        Log.i(TAG, "开始安装...");
        installTaskSubscription = Observable.just(1)
                .subscribeOn(Schedulers.newThread())
                .subscribe(result -> {
                    registerApkInstallReceiver(CommonLib.getInstance().getContext());
                    installedApkCount = 0;
                    for (int i = 0; i < wait2InstallList.size(); i++) {
                        doIntallApk(wait2InstallList.get(i).getLocalPath());
                    }
                });
    }

    private List<UpgradeRequestBean> getApkVersionInfo() {
        UpgradeRequestBean item;
        List<UpgradeRequestBean> softwareVersionInfoList = new ArrayList<>();
        softwareVersionInfoList.clear();

        //获取联新已经安装的APK列表
        List<AppUtils.AppInfo> installedApkList = AppUtils.getAppsInfo(APK_PREFIX);

        for (int i = 0; i < installedApkList.size(); i++) { //获取apk版本信息
            item = new UpgradeRequestBean();
            item.setSoftMark(installedApkList.get(i).getPackageName());
            item.setSoftVersion(0);//installedApkList.get(i).getVersionCode()); debug
            softwareVersionInfoList.add(item);
        }
        return softwareVersionInfoList;
    }

    private void doIntallApk(String apkPath) {
        Log.i(TAG, "开始进行apk安装:" + apkPath);

        ShellUtils.execCmd("pm install -r " + apkPath + "\n",true);
//        try {
//            // 申请su权限
//            Process process = Runtime.getRuntime().exec("su");
//            DataOutputStream dataOutputStream = new DataOutputStream(process.getOutputStream());
//            // 执行pm install命令
//            String command = "pm install -r " + apkPath + "\n";
//            dataOutputStream.write(command.getBytes(Charset.forName("utf-8")));
//            dataOutputStream.flush();
//            dataOutputStream.writeBytes("exit\n");
//            dataOutputStream.flush();
//            process.waitFor();
//            BufferedReader errorStream = new BufferedReader(new InputStreamReader(process.getErrorStream()));
//            String msg = "";
//            String line;
//            // 读取命令的执行结果
//            while ((line = errorStream.readLine()) != null) {
//                msg += line;
//            }
//            Log.d(TAG, "install msg is " + msg);
//            // 如果执行结果中包含Failure字样就认为是安装失败，否则就认为安装成功
//            if (!msg.contains("Failure")) {
//                installResult = true;
//            } else {
//                installResult = false;
//            }
//
//        } catch (Exception e) {
//            Log.e(TAG, "doIntallApk" + e.toString());
//        }

        Log.i(TAG, apkPath+" 安装命令执行完毕:{}"+installResult);

    }

    public void doAfterInstalled(String packageName) {
        packageName = packageName.replace("package:", "");

        Log.i(TAG, packageName + "has been installed!");

        installedApkCount++;

        //删除升级包
        String apkPath = getApkFilePathByName(packageName);
        FileUtils.deleteFile(apkPath);

        if (installedApkCount == downloadApkCount) {
            Log.i(TAG, "全部安装完毕！");
            if (callBack != null) {
                callBack.onComplete();
            } else {

            }

            //通知安装完毕
            EventBus.getDefault().post(new UpgradeCompleteEvent());

            //取消安装监听器
            unRegisterApkInstallReceiver(CommonLib.getInstance().getContext());
        }
    }

    private String getApkFilePathByName(String packageName){
        for(int i=0;i<upgradePackageInfoList.size();i++){
            if(upgradePackageInfoList.get(i).getSoftMark().equals(packageName)){
                return upgradePackageInfoList.get(i).getLocalPath();
            }
        }
        return null;
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

    public void delay2ShowUpradeDialog() {
        delayShowTipSubscription = Observable.timer(10, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    if (delayShowTipSubscription != null && delayShowTipSubscription.isUnsubscribed()) {
                        delayShowTipSubscription.unsubscribe();
                        delayShowTipSubscription = null;
                    }
                    EventBus.getDefault().post(new UpgradeTipEvent(updateNote));
                });
    }

    public void delay2Do(int seconds, Runnable runnable) {
        delay2DoSubscription = Observable.timer(seconds, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    if (delay2DoSubscription != null && delay2DoSubscription.isUnsubscribed()) {
                        delay2DoSubscription.unsubscribe();
                        delay2DoSubscription = null;
                    }

                    runnable.run();
                });
    }

    public static interface UpgradeCallBack {

        public void onComplete();

        public void onError();
    }

    public void downloadFiles(HashMap<String, String> downloadInfoList, final FileDownloadListener queueTarget) {

        boolean isParallel = false; //是否并行下载

//        downloadInfoList.forEach(new BiConsumer<String, String>() {
//            @Override
//            public void accept(String url, String localPath) {

        String url = "http://wap.apk.anzhi.com/data3/apk/201708/02/d28bf178baeb73048226a95be421f65b_35829700.apk";
        String localPath = "/mnt/internal_sd/test.apk";

        FileDownloader.getImpl()
                .create(url)
                .setPath(localPath)
                .setListener(queueTarget)
                .ready();
//            }
//        });


        if (isParallel) {
            // To form a queue with the same queueTarget and execute them in parallel
            FileDownloader.getImpl().start(queueTarget, false);
        } else {
            FileDownloader.getImpl().start(queueTarget, true);
        }
    }


}
