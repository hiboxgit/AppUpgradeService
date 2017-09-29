package com.dudu.upgrade;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.dudu.commonlib.share.constants.BroadcastConstants;
import com.dudu.commonlib.share.http.retrofit.RetrofitFactory;
import com.dudu.commonlib.share.upgrade.ApkUpgradeManager;
import com.dudu.upgrade.fota.policy.PolicyManager;
import com.dudu.upgrade.utils.StorageUtils;
import com.dudu.upgrade.utils.UpdateWindowManager;
import com.fota.iport.MobAgentPolicy;
import com.fota.iport.inter.ICheckVersionCallback;
import com.fota.iport.inter.IOnDownloadListener;
import com.fota.iport.service.DLService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * 检测、下载、安装操作通过此UpgradeService来启动，维持一个前台Service，防止应用进程被系统回收
 * <p>
 * Created by zhongweiguang on 2017/3/10.
 */

public class UpgradeService extends Service {

    private static Logger logger = LoggerFactory.getLogger("dudu_upgrade.UpgradeService");

    private static final String EXTRA_ACTION = "extra_action";

    /**
     * 启动服务用此方法
     */
    public static void startService(Context context, String action) {
        logger.debug("startService action={}", action);
        Intent intent = new Intent(context, UpgradeService.class);
        intent.putExtra(EXTRA_ACTION, action);
        context.startService(intent);
    }

    private boolean systemChecking;        // 正在检测系统更新
    private boolean systemDownloading;     // 正在下载系统更新包

    private PolicyManager policyManager;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        logger.debug("onCreate");

        // 设置为前台进程，防止进程被杀
        Notification notification = new Notification.Builder(this.getApplicationContext())
                .setContentText("这是一个前台服务")
                .build();
        startForeground(141141, notification);

        policyManager = new PolicyManager();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = null;
        if (intent != null) {
            action = intent.getStringExtra(EXTRA_ACTION);
        }
        if (action == null) {
            logger.debug("onStartCommand action=null, return");
            return START_NOT_STICKY;
        }

        switch (action) {
            // 检测更新
            case BroadcastConstants.ACTION_DUDU_FOTA_CHECK_START: {
                logger.debug("onStartCommand action={}", "ACTION_START_CHECK_UPDATE(检测更新)");
                checkUpdate();
                break;
            }
            // 检测应用更新完成，包括检测成功和检测失败
            case BroadcastConstants.ACTION_DUDU_APP_CHECK_FINISHED: {
                logger.debug("onStartCommand action={}", "ACTION_CHECK_APP_UPDATE_FINISHED(检测应用更新完成，包括检测成功和检测失败)");
                handleCheckAppUpdateFinished();
                break;
            }
            // 安装系统更新
            case BroadcastConstants.ACTION_DUDU_FOTA_UPGRADE_START: {
                logger.debug("onStartCommand action={}", "ACTION_INSTALL_SYSTEM_PACKAGE(安装系统更新)");
                rebootInstallSystemUpdatePackage();
                break;
            }
            // 安装系统更新fordebug
            case BroadcastConstants.ACTION_DUDU_FOTA_UPGRADE_DEBUG_START: {
                logger.debug("onStartCommand action={}", "ACTION_INSTALL_SYSTEM_PACKAGE(安装系统更新debug)");
                rebootInstallSystemUpdatePackage1();
                break;
            }
            // 安装应用更新
            case BroadcastConstants.ACTION_DUDU_APP_UPGRADE_START: {
                logger.debug("onStartCommand action={}", "ACTION_INSTALL_APK(安装应用更新)");
                installUpdateApps();
                break;
            }
            // 安装应用更新完成
            case BroadcastConstants.ACTION_DUDU_APP_INSTALL_FINISHED: {
                logger.debug("onStartCommand action={}", "ACTION_DUDU_APP_INSTALL_FINISHED(安装应用更新完成,开始重启)");
                handleInstallUpdateAppsFinished();
                break;
            }
            default:
                logger.debug("onStartCommand action={}, do nothing", action);
                break;
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        logger.debug("onDestroy");
    }

    /**
     * 检测更新
     * <p>
     * 1、会先检测系统更新
     * 2、如果系统检测有更新则不会再检测应用更新
     * 3、如果系统没有更新或者检测更新出错时，会开始检测应用更新（检测到应用有更新目前是自动下载安装包）
     */
    private void checkUpdate() {
        if (systemChecking || systemDownloading || ApkUpgradeManager.getInstance().isCheckingOrDownloading() ||
                ApkUpgradeManager.getInstance().isInstalling()) {
            logger.debug("checkUpdate return, systemChecking={}, systemDownloading={}, isCheckingOrDownloading(应用)={}, isInstalling(应用)={}",
                    systemChecking, systemDownloading,
                    ApkUpgradeManager.getInstance().isCheckingOrDownloading(),
                    ApkUpgradeManager.getInstance().isInstalling());
            return;
        }

        systemChecking = true;
        RetrofitFactory.showDebugLog(true); // Debug版本打印OkHttp Log

        MobAgentPolicy.checkVersion(getApplicationContext(), new ICheckVersionCallback() {
            @Override
            public void onCheckSuccess(int status) {
                logger.debug("checkVersion onCheckSuccess(系统更新) statue={}", status);

                Intent intent = new Intent(BroadcastConstants.ACTION_DUDU_FOTA_CHECK_SUCCESS);
                sendBroadcast(intent);
                logger.debug("checkVersion onCheckSuccess(系统更新) sendBroadcast action={}", intent.getAction());

                logger.debug("checkVersion onCheckSuccess(系统更新), 检测到系统更新, 开始下载系统更新包...");
                downloadSystemUpdatePackage();
                systemChecking = false;
            }

            @Override
            public void onCheckFail(int status, String errorMsg) {
                logger.debug("checkVersion onCheckFail(系统更新) statue={}, errorMsg={}", status, errorMsg);

                Intent intent = new Intent(BroadcastConstants.ACTION_DUDU_FOTA_CHECK_ERROR);
                sendBroadcast(intent);
                logger.debug("checkVersion onCheckFail(系统更新), sendBroadcast action={}", intent.getAction());

                try {
                    logger.debug("checkVersion onCheckFail(system), 未检测到系统更新, 开始检测应用更新...");
                    ApkUpgradeManager.getInstance().updateAllPackages();
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.debug("检测应用更新出错, exception={}", e.toString());

                    // 停止服务，防止进程无法回收
                    logger.debug("停止服务，防止进程无法回收");
                    stopSelf();
                }
                systemChecking = false;
            }

            @Override
            public void onInvalidDate() {
                logger.debug("checkVersion onInvalidDate(系统更新)");

                Intent intent = new Intent(BroadcastConstants.ACTION_DUDU_FOTA_CHECK_ERROR);
                sendBroadcast(intent);
                logger.debug("checkVersion onInvalidDate(系统更新), sendBroadcast action={}", intent.getAction());

                try {
                    logger.debug("checkVersion onInvalidDate(系统更新), 检测系统更新出错, 开始检测应用更新...");
                    ApkUpgradeManager.getInstance().updateAllPackages();
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.debug("检测应用更新出错, exception={}", e.toString());

                    // 停止服务，防止进程无法回收
                    logger.debug("停止服务，防止进程无法回收");
                    stopSelf();
                }
                systemChecking = false;
            }
        });
    }

    /**
     * 下载系统更新安装包
     */
    private void downloadSystemUpdatePackage() {
        systemDownloading = true;

        File file = new File(getSystemUpdatePackagePath());
        logger.debug("准备下载系统更新包, download path={}", file.getAbsolutePath());

        DLService.start(getApplicationContext(),
                MobAgentPolicy.getVersionInfo().deltaUrl,   // 下载url
                new File(file.getParent()), // 文件父目录
                file.getName(), // 文件名
                new IOnDownloadListener() {
                    @Override
                    public void onDownloadProgress(String tmpPath, int totalSize, int downloadedSize) {
                        int progress = (int) (100 * (downloadedSize / (float) totalSize));
                        logger.debug("正在下载系统更新包..., tmpPath={}, totalSize={}, downloadedSize={}, progress={}",
                                tmpPath, totalSize, downloadedSize, progress);

                        Intent intent = new Intent(BroadcastConstants.ACTION_DUDU_FOTA_DOWNLOAD_PROGRESS);
                        intent.putExtra(BroadcastConstants.KEY_INTENT_ARG_DUDU_FOTA_DOWNLOAD_PROGRESS, progress);
                        sendBroadcast(intent);
//                        logger.debug("正在下载系统更新包..., sendBroadcast action={}, progress={}", intent.getAction(), progress);
                    }

                    @Override
                    public void onDownloadFinished(int state, File file) {
                        logger.debug("下载系统更新包完成, state={}, file path={}", state, file.getAbsolutePath());

                        Intent intent = new Intent(BroadcastConstants.ACTION_DUDU_FOTA_DOWNLOAD_FINISHED);
                        intent.putExtra(BroadcastConstants.KEY_INTENT_ARG_DUDU_FOTA_RELEASE_NOTES, MobAgentPolicy.getRelNotesInfo().content);
                        intent.putExtra(BroadcastConstants.KEY_INTENT_ARG_DUDU_FOTA_RELEASE_DATE, MobAgentPolicy.getRelNotesInfo().publishDate);
                        sendBroadcast(intent);
                        logger.debug("下载系统更新包完成, sendBroadcast action={}, update message={}",
                                intent.getAction(), MobAgentPolicy.getRelNotesInfo().content);

                        systemDownloading = false;

                        // 停止服务，防止进程无法回收
                        logger.debug("停止服务，防止进程无法回收");
                        stopSelf();
                    }

                    @Override
                    public void onDownloadError(int error) {
                        logger.debug("下载系统更新包出错, error={}", error);

                        Intent intent = new Intent(BroadcastConstants.ACTION_DUDU_FOTA_DOWNLOAD_ERROR);
                        sendBroadcast(intent);
                        logger.debug("下载系统更新包出错, sendBroadcast action={}", intent.getAction());
                        
                        systemDownloading = false;

                        // 停止服务，防止进程无法回收
                        logger.debug("停止服务，防止进程无法回收");
                        stopSelf();
                    }
                }
        );
    }

    /**
     * 获取系统更新包下载路径
     */
    private String getSystemUpdatePackagePath() {
        String path;
        
        // 后台配置的下载路径
        String configPath = policyManager.get_storage_path();
        if (!TextUtils.isEmpty(configPath)) {
            path = configPath;
        } else {
            path = StorageUtils.get_update_file_path(this);
        }
        
        return path;
    }

    /**
     * 重启系统并安装系统更新
     */
    private void rebootInstallSystemUpdatePackage() {
        try {
            String filePath = getSystemUpdatePackagePath();
            logger.debug("重启系统并安装系统更新包..., file path={}", filePath);
            MobAgentPolicy.rebootUpgrade(getApplicationContext(), filePath);

        } catch (Exception e) {
            e.printStackTrace();
            logger.debug("安装系统更新出错, exception={}", e.toString());

            // 停止服务，防止进程无法回收
            logger.debug("停止服务，防止进程无法回收");
            stopSelf();
        }
    }
    /**
     * 重启系统并安装系统更新for debug
     */
    private void rebootInstallSystemUpdatePackage1() {
        try {
            String filePath = "/storage/sdcard1/update.zip";
            File file = new File(filePath);
            logger.debug("在后门重启系统并安装系统更新包debug..., file path={}", filePath);
            if(file.exists()) {
                MobAgentPolicy.rebootUpgrade(getApplicationContext(), filePath);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.debug("安装系统更新出错, exception={}", e.toString());

            // 停止服务，防止进程无法回收
            logger.debug("停止服务，防止进程无法回收");
            stopSelf();
        }
    }
    /**
     * 检测应用更新完成，包括检测成功和检测失败
     */
    private void handleCheckAppUpdateFinished() {
        logger.debug("检测应用更新完成，包括检测成功和检测失败");
        // 停止服务，防止进程无法回收
        logger.debug("停止服务，防止进程无法回收");
        stopSelf();
    }

    /**
     * 安装应用更新
     */
    private void installUpdateApps() {
        try {
            logger.debug("开始安装应用更新");
            UpdateWindowManager.getInstance().show();
            ApkUpgradeManager.getInstance().installAllUpgradePackages();
        } catch (Exception e) {
            e.printStackTrace();
            logger.debug("安装应用更新出错, exception={}", e.toString());
            UpdateWindowManager.getInstance().dismiss();

            // 停止服务，防止进程无法回收
            logger.debug("停止服务，防止进程无法回收");
            stopSelf();
        }
    }

    /**
     * 安装应用更新完成，重启系统
     */
    private void handleInstallUpdateAppsFinished() {
        logger.debug("安装应用更新完成，重启系统...");
        PowerManager powerManager = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
        powerManager.reboot("");
    }

}
