package com.lachesis.appupgradeservice.modules.upgrade.controller.core;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.lachesis.appupgradeservice.modules.upgrade.controller.broadcast.InstallEventReceiver;
import com.lachesis.appupgradeservice.modules.upgrade.handler.HttpUpgradeHandler;
import com.lachesis.appupgradeservice.modules.upgrade.model.UpgradeConfigInfo;
import com.lachesis.appupgradeservice.modules.upgrade.model.UpgradePackageInfo;
import com.lachesis.appupgradeservice.modules.upgrade.model.UpgradeRequestBean;
import com.lachesis.appupgradeservice.modules.upgrade.model.UpgradeResponseBean;
import com.lachesis.appupgradeservice.modules.upgrade.view.UpgradeViewModel;
import com.lachesis.appupgradeservice.share.RunDataHelper;
import com.lachesis.common.CommonLib;
import com.lachesis.common.base.IBaseAsyncHandler;
import com.lachesis.common.network.HttpTool;
import com.lachesis.common.network.bean.DownloaderConfigBean;
import com.lachesis.common.utils.AppUtils;
import com.lachesis.common.utils.FileUtils;
import com.lachesis.common.utils.SDCardUtils;
import com.lachesis.common.utils.ShellUtils;
import com.lachesis.common.utils.TaskUtils;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadListener;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscription;
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
    private String updateNote = "升级内容";
    private boolean hasFailedInstallTask;
    private UpgradeCallBack callBack;
    private InstallEventReceiver installEventReceiver;

    private Subscription timerCheckTaskSubscription;
    private Subscription hostCheckTaskSubscription;
    private Subscription initCheckUpdateSubscription;
    private Subscription startUpgradeTipSubscription;
    private Subscription checkUpdateSubscription;
    private Subscription installTaskSubscription;
    private Subscription delay2DoSubscription;

    List<UpgradePackageInfo> upgradePackageInfoList = new ArrayList<>();
    private List<UpgradeResponseBean> upgradeResponseList;
    private List<UpgradeConfigInfo> wait2InstallList;
    private int downloadApkCount;
    private int installedApkCount;

    private UpgradeViewModel upgradeViewModel;

    public AppUpgradeManager() {
        installEventReceiver = new InstallEventReceiver();
        upgradeViewModel = new UpgradeViewModel(CommonLib.getInstance().getContext());
    }

    public static AppUpgradeManager getInstance() {
        return instance;
    }

    public UpgradeViewModel getUpgradeViewModel() {
        return upgradeViewModel;
    }

    public void setUpgradeViewModel(UpgradeViewModel upgradeViewModel) {
        this.upgradeViewModel = upgradeViewModel;
    }

    public void upgrade(UpgradeCallBack callBack) {
        this.callBack = callBack;
        upgrade();
    }

    public void upgrade() {
        Log.i(TAG, "开始升级...");
        cancelTimerCheckTask();
        timerCheckTaskSubscription = TaskUtils.interval(1, 60)
                .observeOn(Schedulers.newThread())
                .subscribe(s -> {
                    Log.i(TAG, "定时检查更新，到点了:" + s);
                    if (RunDataHelper.getInstance().hasValidServerHost()) {
                        Log.i(TAG, "升级服务器已经配置且有效");
                        checkUpdate();
                    } else {
                        cancelHostCheckTask();
                        hostCheckTaskSubscription = TaskUtils.runMainThread()
                                .subscribe(s1 -> {
                                    getUpgradeViewModel().onSetServer(new IBaseAsyncHandler() {
                                        @Override
                                        public void onSuccess(Object result) {
                                            cancelInitCheckUpdateTask();
                                            initCheckUpdateSubscription = TaskUtils.runSubThread()
                                                    .subscribe(s2 -> {
                                                        checkUpdate();
                                                    });
                                        }

                                        @Override
                                        public void onError(Object result) {

                                        }

                                        @Override
                                        public void onComplete(Object result) {

                                        }
                                    });
                                });
                    }
                });
    }

    private void cancelTimerCheckTask() {
        if (timerCheckTaskSubscription != null && !timerCheckTaskSubscription.isUnsubscribed()) {
            timerCheckTaskSubscription.unsubscribe();
            timerCheckTaskSubscription = null;
        }
    }//

    private void cancelHostCheckTask() {

        if (hostCheckTaskSubscription != null && !hostCheckTaskSubscription.isUnsubscribed()) {
            hostCheckTaskSubscription.unsubscribe();
            hostCheckTaskSubscription = null;
        }
    }

    private void cancelInitCheckUpdateTask() {

        if (initCheckUpdateSubscription != null && !initCheckUpdateSubscription.isUnsubscribed()) {
            initCheckUpdateSubscription.unsubscribe();
            initCheckUpdateSubscription = null;
        }
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
                        Log.i(TAG, "无有效更新内容！");
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

        Log.i(TAG, "准备循环...");
        for (int i = 0; i < upgradeInfoList.size(); i++) {
            Log.i(TAG, "循环更新待升级包..." + i);
            String softMark = upgradeInfoList.get(i).getSoftMark();
            String apkName = upgradeInfoList.get(i).getSoftName();

            String localPath = SDCardUtils.getSDCardPath() + "ApkFiles/" + i + ".apk";
            String host = RunDataHelper.getInstance().getServerHostConfig();
            String remoteUrl = host.substring(0, host.length() - 1) + upgradeInfoList.get(i).getSoftPath();
            upgradePackageInfoList.add(new UpgradePackageInfo(softMark, remoteUrl, localPath));

            Log.i(TAG, "添加到待升级列表->localPath:" + localPath + ",remoteUrl:" + remoteUrl);

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
//        EventBus.getDefault().post(new UpgradeTipEvent(updateNote));
        startUpgradeTipSubscription = TaskUtils.runMainThread()
                .subscribe(s -> {
                    getUpgradeViewModel().onShowUpgradeTip();
                });
    }

    public void install() {
        Log.i(TAG, "开始安装...");
        installTaskSubscription = Observable.just(1)
                .subscribeOn(Schedulers.newThread())
                .subscribe(result -> {
                    //先检查授权
                    ShellUtils.execCmd("echo test\n", true);
                    getUpgradeViewModel().onShowLoading();

                    //然后再开始安装
                    registerApkInstallReceiver(CommonLib.getInstance().getContext());
                    installedApkCount = 0;
                    hasFailedInstallTask = false;
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

        ShellUtils.CommandResult result = ShellUtils.execCmd("pm install -r " + apkPath + "\n", true);
        Log.i(TAG, "result.result:" + result.result);
        Log.i(TAG, apkPath + " 安装命令执行完毕 -> successMsg:" + result.successMsg + ",errorMsg:" + result.errorMsg);
        if (result.result == 0) {
            //能够执行安装
        } else {
            //无法执行安装
            hasFailedInstallTask = true; //被标记过，就代表失败过。
            doAfterInstalled(getPackageNameByFilePath(apkPath));
        }
    }

    public void doAfterInstalled(String packageName) {
        packageName = packageName.replace("package:", "");

        Log.i(TAG, packageName + " has been installed!");

        installedApkCount++;

        //删除升级包
        String apkPath = getApkFilePathByName(packageName);
        Log.i(TAG, "准备删除已经安装过的升级包:" + apkPath);
        FileUtils.deleteFile(apkPath);

        if (installedApkCount == downloadApkCount) {
            Log.i(TAG, "全部安装完毕！");
            if (callBack != null) {
                callBack.onComplete();
            } else {

            }

            //通知安装完毕
            getUpgradeViewModel().onShowComplete();

            //取消安装监听器
            unRegisterApkInstallReceiver(CommonLib.getInstance().getContext());
        }
    }

    private String getApkFilePathByName(String packageName) {
        for (int i = 0; i < upgradePackageInfoList.size(); i++) {
            if (upgradePackageInfoList.get(i).getSoftMark().equals(packageName)) {
                return upgradePackageInfoList.get(i).getLocalPath();
            }
        }
        return null;
    }

    private String getPackageNameByFilePath(String filePath) {
        for (int i = 0; i < upgradePackageInfoList.size(); i++) {
            if (upgradePackageInfoList.get(i).getLocalPath().equals(filePath)) {
                return upgradePackageInfoList.get(i).getSoftMark();
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

    public static interface UpgradeCallBack {

        public void onComplete();

        public void onError();
    }
}
