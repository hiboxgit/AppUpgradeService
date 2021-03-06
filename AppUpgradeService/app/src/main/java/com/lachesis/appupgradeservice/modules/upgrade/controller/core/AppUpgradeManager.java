package com.lachesis.appupgradeservice.modules.upgrade.controller.core;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.util.Log;

import com.lachesis.appupgradeservice.modules.upgrade.controller.broadcast.InstallEventReceiver;
import com.lachesis.appupgradeservice.modules.upgrade.handler.UpdateHandler;
import com.lachesis.appupgradeservice.modules.upgrade.model.UpgradePackageInfo;
import com.lachesis.appupgradeservice.modules.upgrade.model.UpgradeRequest;
import com.lachesis.appupgradeservice.modules.upgrade.model.UpgradeResponse;
import com.lachesis.appupgradeservice.modules.upgrade.view.UpgradeViewModel;
import com.lachesis.appupgradeservice.share.AppConfig;
import com.lachesis.appupgradeservice.share.RunDataHelper;
import com.lachesis.common.CommonLib;
import com.lachesis.common.base.IBaseAsyncHandler;
import com.lachesis.common.network.HttpTool;
import com.lachesis.common.network.bean.DownloaderConfigBean;
import com.lachesis.common.utils.AppUtils;
import com.lachesis.common.utils.FileUtils;
import com.lachesis.common.utils.ShellUtils;
import com.lachesis.common.utils.TaskUtils;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private Logger logger = LoggerFactory.getLogger("LxAppUpgrade");

    private static AppUpgradeManager instance = new AppUpgradeManager();

    private static final String APK_PREFIX = "com.lachesis"; //用于找出指定包名前缀的已安装APK
    public String updateNote = "升级内容";
    private boolean hasFailedInstallTask;
    private UpgradeCallBack callBack;
    private InstallEventReceiver installEventReceiver;

    private Subscription timerCheckTaskSubscription;
    private Subscription hostCheckTaskSubscription;
    private Subscription initCheckUpdateSubscription;
    private Subscription checkUpdateSubscription;
    private Subscription installTaskSubscription;
    private Subscription getDelayTimeTaskSubscription;
    private Subscription delayEndUgpradeSubscription;

    List<UpgradePackageInfo> upgradePackageInfoList;
    //    private int downloadTaskCount; //下载任务执行完毕了几个，不论成功或者失败都认为是完成了
    private int downloadSuccessCount;
    private int downloadFailCount;
    private int installTaskCount;

    private UpgradeViewModel upgradeViewModel;

    /* 专为广播通知的单个APK主动更新请求 */
    private boolean isUpgradeRunning;
    private boolean isAssignUpgrade;
    private String assignUpgradePackageName;
    private boolean isHideInstall;

    public AppUpgradeManager() {
        installEventReceiver = new InstallEventReceiver();
        upgradeViewModel = new UpgradeViewModel(CommonLib.getInstance().getContext());
        upgradePackageInfoList = new ArrayList<>();
        isUpgradeRunning = false;
        isHideInstall = true;
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
        logger.info("升级管理启动了...");
        cancelTimerCheckTask();
        int checkInterval = RunDataHelper.getInstance().getCheckInterval();
        timerCheckTaskSubscription = TaskUtils.interval(0, checkInterval)
                .observeOn(Schedulers.newThread())
                .subscribe(s -> {
                    logger.info("定时检查更新，到点了:" + s);
                    if (!isUpgradeRunning) {
                        isAssignUpgrade = false;
                        upgradeStart();
                    } else {
                        logger.info("已经有升级在跑了...");
                    }

                });
    }

    public void upgradeByPackageName(String packageName) {
        if (isUpgradeRunning) {
            logger.info("上次升级任务还未结束!");
            return;
        }
        isAssignUpgrade = true;
        assignUpgradePackageName = packageName;
        upgradeStart();
    }

    private void upgradeStart() {
        onUpgradeStartRun();
        if (RunDataHelper.getInstance().hasValidServerHost()) {
            logger.info("升级服务器已经配置且有效");
            checkUpdate();
        } else {
            cancelHostCheckTask();
            getUpgradeViewModel().onSetServer(new IBaseAsyncHandler() {
                @Override
                public void onSuccess(Object result) {

                    if (RunDataHelper.getInstance().hasValidServerHost()) {
                        cancelInitCheckUpdateTask();
                        initCheckUpdateSubscription = TaskUtils.runSubThread()
                                .subscribe(s2 -> {
                                    checkUpdate();
                                });
                    } else {
                        Log.e(TAG, "服务器地址配置错误，不进行更新检查");
                        onFailConnect();
                    }
                }

                @Override
                public void onError(Object result) {
                    onUpgradeStopRun();
                }

                @Override
                public void onComplete(Object result) {

                }
            });

        }
    }

    public void cancelTimerCheckTask() {
        if (timerCheckTaskSubscription != null && !timerCheckTaskSubscription.isUnsubscribed()) {
            timerCheckTaskSubscription.unsubscribe();
            timerCheckTaskSubscription = null;
        }
    }

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
        logger.info("开始检查更新...");
        queryInstallDelayTime(); //首先获取一下新的安装延迟时间
        List<UpgradeRequest> requestData = getApkVersionInfo();
        checkUpdateSubscription = UpdateHandler.checkApkUpdate(requestData)
                .subscribeOn(Schedulers.io())
                .subscribe(result -> {
                    logger.info("接收到升级响应");

                    if (result == null || result.size() == 0) {
                        logger.info("无有效更新内容！");
                        onUpgradeStopRun();
                    } else {
                        List<UpgradeResponse> upgradeResponseList = new ArrayList<UpgradeResponse>();
                        upgradeResponseList.clear();
                        for (int i = 0; i < result.size(); i++) {
                            if (result.get(i).getSoftPath() != null && !result.get(i).getSoftPath().equals("")) {
                                upgradeResponseList.add(result.get(i));
                            }
                        }
                        logger.info("接收到的有效升级地址列表个数为：" + upgradeResponseList.size());
                        if (upgradeResponseList.size() > 0) {
                            getRemoteUpdateInfo(upgradeResponseList);
                        } else {
                            logger.info("无可用升级包！");
                            onUpgradeStopRun();
                            if (isAssignUpgrade) {
                                onNoUpdate();
                            }
                        }
                    }

                }, throwable -> {
                    onFailConnect();
                });
    }

    private void queryInstallDelayTime(){
        if(getDelayTimeTaskSubscription != null && !getDelayTimeTaskSubscription.isUnsubscribed()){
            getDelayTimeTaskSubscription.unsubscribe();
            getDelayTimeTaskSubscription = null;
        }
        getDelayTimeTaskSubscription = UpdateHandler.getUpgradeDelayTime()
                .subscribeOn(Schedulers.io())
                .subscribe(result -> {
                    logger.info("安装延迟时间配置查询结果为：{}", result.toString());
                    if (result.getConfigCode().equals("softdelayTime")) {
                        String delayTimeStr = result.getConfigValue();
                        int delayTime = Integer.valueOf(delayTimeStr);
                        RunDataHelper.getInstance().setDelayInstallTime(delayTime);
                    }

                }, throwable -> {
                    logger.error("查询延迟时间异常：{}", throwable.toString());
                }, () -> {

                });
    }
    private void onFailConnect() {
        getUpgradeViewModel().onFailSetServer(new IBaseAsyncHandler() {
            @Override
            public void onSuccess(Object result) {
                checkUpdate();
            }

            @Override
            public void onError(Object result) {
                onUpgradeStopRun();
            }

            @Override
            public void onComplete(Object result) {

            }
        });

    }

    private void getRemoteUpdateInfo(List<UpgradeResponse> upgradeInfoList){
        logger.info("开始过滤远程升级信息...:" + upgradeInfoList.size());
        upgradePackageInfoList.clear();

        logger.info("准备收集可用升级包...");
        String host = RunDataHelper.getInstance().getServerHostConfig();
        for (int i = 0; i < upgradeInfoList.size(); i++) {
            logger.info("循环更新待升级包..." + i);
            String softMark = upgradeInfoList.get(i).getSoftMark();
            String localPath = AppConfig.APK_DOWNLOAD_DIR + i + ".apk";
            String remoteUrl = host.substring(0, host.length() - 1) + upgradeInfoList.get(i).getSoftPath();
            String dsc = upgradeInfoList.get(i).getSoftDesc();
            Boolean isForeground = AppUtils.isAppForeground(CommonLib.getInstance().getContext(), softMark);
            logger.info("准备添加到待升级列表->localPath:" + localPath + ",remoteUrl:" + remoteUrl + ",softMark:" + softMark);
            UpgradePackageInfo packItem = new UpgradePackageInfo(softMark, remoteUrl, localPath, dsc);
            packItem.setForeground(isForeground);
            if (!isAssignUpgrade) {
                upgradePackageInfoList.add(packItem);
            } else {
                if (assignUpgradePackageName.equals(softMark)) {
                    upgradePackageInfoList.add(packItem);
                }
            }
        }

        logger.info("可用升级信息大小:{}",upgradePackageInfoList.size());
        if (upgradePackageInfoList.size() <= 0) {
            logger.info("没有下载任务可用,结束本次升级...");
            onUpgradeStopRun();
            if (isAssignUpgrade) {
                onNoUpdate();
            }
            return;
        } else {
            updateNote = "";
            for (int i = 0; i < upgradePackageInfoList.size(); i++) {
                String dsc = upgradePackageInfoList.get(i).getUpdateDsc();//debug
                if (dsc == null || dsc.equals("") || dsc.equals("null") || dsc.equals(" ")) {
                    continue;
                }
                if (i > 0) {
                    updateNote = updateNote + "\n";
                }
                updateNote = updateNote + dsc;
            }
        }

        if (!isAssignUpgrade) {
            getUpgradeViewModel().onShowUpgradeTip();
        } else {
            getUpgradeViewModel().onShowUpgradeConfirmTip();
        }
    }
    public void download() {
        logger.info("开始下载...");
        //先检查授权
        ShellUtils.CommandResult commandResult = ShellUtils.execCmd("echo test\n", true);
        if (commandResult.result != 0) {
            logger.info("无法获取root权限，采用显式升级");
            isHideInstall = false;
        } else {
            isHideInstall = true;
            getUpgradeViewModel().onShowLoading();
        }

        //下载队列任务开始
        downloadSuccessCount = 0;
        downloadFailCount = 0;
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
                logger.info("升级包" + task.getTag() + "下载pending");
            }

            @Override
            protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {

            }

            @Override
            protected void blockComplete(BaseDownloadTask task) {
                logger.info("blockComplete ");
            }

            @Override
            protected void completed(BaseDownloadTask task) {
                logger.info("升级包" + task.getTag() + "下载完成 ");
                updateDownloadStatus(task.getTag().toString(), true);
                downloadSuccessCount++;
                doOnEachDownloadTaskComplete();
            }

            @Override
            protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                logger.info("升级包" + task.getTag() + "下载暂停 ");
            }

            @Override
            protected void error(BaseDownloadTask task, Throwable e) {
                logger.info("升级包" + task.getTag() + "下载失败");
                updateDownloadStatus(task.getTag().toString(), false);
                downloadFailCount++;
                doOnEachDownloadTaskComplete();
            }

            @Override
            protected void warn(BaseDownloadTask task) {
                logger.info("升级包" + task.getTag() + "下载警告 ");
            }
        });
    }

    private void updateDownloadStatus(String tag, boolean isDownloaded) {
        for (int i = 0; i < upgradePackageInfoList.size(); i++) {
            if (upgradePackageInfoList.get(i).getSoftMark().equals(tag)) {
                upgradePackageInfoList.get(i).setDownloaded(isDownloaded);
                return;
            }
        }
    }

    private int countDownloadedSuccessPackages() {
        int count = 0;
        for (int i = 0; i < upgradePackageInfoList.size(); i++) {
            if (upgradePackageInfoList.get(i).isDownloaded()) {
                count++;
            }
        }
        return count;
    }

    private void doOnEachDownloadTaskComplete() {
        int downloadTaskCount = downloadSuccessCount + downloadFailCount;
        if (upgradePackageInfoList.size() == downloadTaskCount) {
            //全部下载完毕
            doOnCompleteDownload();
        }
    }

    private void doOnCompleteDownload() {
        logger.info("全部下载完成了,downloadSuccessCount:{},downloadFailCount:{}", downloadSuccessCount, downloadFailCount);
        //查询延迟时间完毕开始进行弹窗提示升级
        if (downloadSuccessCount > 0) {
            install();
        } else if (downloadFailCount > 0 && downloadSuccessCount == 0) {
            getUpgradeViewModel().onShowDownloadFailTip();
            onUpgradeStopRun();
        }
    }

    public void install() {
        logger.info("开始安装...");
        installTaskSubscription = Observable.just(1)
                .subscribeOn(Schedulers.newThread())
                .subscribe(result -> {

                    //然后再开始安装
                    registerApkInstallReceiver(CommonLib.getInstance().getContext());
                    installTaskCount = 0;
                    hasFailedInstallTask = false;

                    if (countDownloadedSuccessPackages() == 0) {
                        onUpgradeStopRun();
                        return;
                    }
                    for (int i = 0; i < upgradePackageInfoList.size(); i++) {
                        if (upgradePackageInfoList.get(i).isDownloaded()) {
                            if (isHideInstall) {
                                doIntallApkHide(upgradePackageInfoList.get(i).getLocalPath());
                            } else {
                                doIntallApkDefault(upgradePackageInfoList.get(i).getLocalPath());
                            }

                        }
                    }
                });

        //防止一些异常情况，收不到安装完毕广播，超时自动设置成安装完毕。
        delayEndUpgrade();
    }

    private void delayEndUpgrade(){
        if(delayEndUgpradeSubscription != null && !delayEndUgpradeSubscription.isUnsubscribed()){
            delayEndUgpradeSubscription.unsubscribe();
            delayEndUgpradeSubscription = null;
        }

        delayEndUgpradeSubscription = TaskUtils.delay2Do(60*3)
                .subscribe(s->{
                    if(isUpgradeRunning){
                        logger.info("超时未完成安装，自动设置为完成...");
                        getUpgradeViewModel().onShowInstallFailTip();
                        onUpgradeStopRun();

                    }
                });
    }

    private List<UpgradeRequest> getApkVersionInfo() {
        UpgradeRequest item;
        List<UpgradeRequest> softwareVersionInfoList = new ArrayList<>();
        softwareVersionInfoList.clear();

        //获取联新已经安装的APK列表
        List<AppUtils.AppInfo> installedApkList = AppUtils.getAppsInfo(APK_PREFIX);

        if (AppConfig.isDebug) {
            logger.info("当前调试状态，添加调试软件列表...");
            item = new UpgradeRequest();
            item.setSoftMark("com.lachesis.ibis_n");
            item.setSoftVersion(0);
            softwareVersionInfoList.add(item);

            item = new UpgradeRequest();
            item.setSoftMark("com.lachesis.ibis_r");
            item.setSoftVersion(0);
            softwareVersionInfoList.add(item);

            item = new UpgradeRequest();
            item.setSoftMark("com.lachesis.ibis_p");
            item.setSoftVersion(0);
            softwareVersionInfoList.add(item);

        } else {
            for (int i = 0; i < installedApkList.size(); i++) { //获取apk版本信息
                item = new UpgradeRequest();
                item.setSoftMark(installedApkList.get(i).getPackageName());
                item.setSoftVersion(installedApkList.get(i).getVersionCode()); //0);//debug //
                softwareVersionInfoList.add(item);
            }
        }
        return softwareVersionInfoList;
    }

    private void doIntallApkDefault(String filePath) {
        String FILE_NAME = "file://";
        String APPLICATION_NAME = "application/vnd.android.package-archive";
        try {
            Intent intent = new Intent("android.intent.action.VIEW");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setDataAndType(Uri.parse(FILE_NAME + filePath), APPLICATION_NAME);
            CommonLib.getInstance().getContext().startActivity(intent);
        } catch (Exception e) {
            logger.error("显式安装失败:{}", e.toString());
        }

    }

    private void doIntallApkHide(String apkPath) {
        logger.info("开始进行apk安装:" + apkPath);

        ShellUtils.CommandResult result = ShellUtils.execCmd("pm install -r " + apkPath + "\n", true);
        logger.info("result.result:" + result.result);
        logger.info(apkPath + " 安装命令执行完毕 -> successMsg:" + result.successMsg + ",errorMsg:" + result.errorMsg);
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

        logger.info(packageName + " has been installed!");

        installTaskCount++;

        //设置成已安装状态
        for (int i = 0; i < upgradePackageInfoList.size(); i++) {
            if (upgradePackageInfoList.get(i).getSoftMark().equals(packageName)) {
                upgradePackageInfoList.get(i).setInstalled(true);
                break;
            }
        }

        //删除升级包
        String apkPath = getApkFilePathByName(packageName);
        logger.info("开始删除已经安装过的升级包:{}", apkPath);
        FileUtils.deleteFile(apkPath);

        if (installTaskCount == countDownloadedSuccessPackages()) {
            logger.info("全部安装完毕！");
            if (callBack != null) {
                callBack.onComplete();
            } else {

            }

            //取消安装监听器
            unRegisterApkInstallReceiver(CommonLib.getInstance().getContext());

            //通知安装完毕
            getUpgradeViewModel().onShowComplete();

            //拉起之前的前台app
            logger.info("检查是否有前台需要拉起的app...");
            for (int i = 0; i < upgradePackageInfoList.size(); i++) {
                if (upgradePackageInfoList.get(i).isForeground() &&
                        upgradePackageInfoList.get(i).isDownloaded() &&
                        upgradePackageInfoList.get(i).isInstalled()) {
                    logger.info("开始拉起之前的前台app...{}", upgradePackageInfoList.get(i).getSoftMark());
                    AppUtils.launchApp(CommonLib.getInstance().getContext(), upgradePackageInfoList.get(i).getSoftMark());
                    break;
                }
            }
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
        logger.info("动态注册APK安装监听器");
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
        logger.info("取消APK安装监听器");
        try {
            context.unregisterReceiver(installEventReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onUpgradeStartRun() {
        isUpgradeRunning = true;
    }

    public void onUpgradeStopRun() {
        isUpgradeRunning = false;
    }

    private void onNoUpdate() {
        getUpgradeViewModel().onShowNoUpgradeTip();
    }

    public static interface UpgradeCallBack {

        public void onComplete();

        public void onError();
    }
}
