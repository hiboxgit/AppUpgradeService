package com.lachesis.appupgradeservice.modules.upgrade.controller.service;

import android.app.Dialog;
import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.view.WindowManager;

import com.lachesis.appupgradeservice.modules.upgrade.controller.core.AppUpgradeManager;
import com.lachesis.appupgradeservice.modules.upgrade.model.UpgradeCompleteEvent;
import com.lachesis.appupgradeservice.modules.upgrade.model.UpgradeTipEvent;
import com.lachesis.appupgradeservice.share.BroadCastConstants;
import com.lachesis.appupgradeservice.share.Constants;
import com.lachesis.common.ui.dialog.LoadingDialog;
import com.lachesis.common.ui.dialog.SimpleDialog;
import com.lachesis.common.utils.TaskUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

/**
 * app升级管理类
 * <p>
 * Created by Robert on 2017/9/19.
 */
public class UpgradeService extends Service {

    private static String TAG = "UpgradeService";

    private SimpleDialog upgradeTipDialog;
    private LoadingDialog loadingDialog;
    private SimpleDialog completeTipDialog;

    private Subscription countTaskSubscription;

    public UpgradeService() {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        setForegroundService();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        String action = null;
        if (intent != null) {
            action = intent.getAction();
        }
        if (action == null) {
            Log.i(TAG, "onStartCommand action=null, return");
            return START_NOT_STICKY;
        }

        Log.i(TAG,"onStartCommand:"+action);
        switch (action) {

            case BroadCastConstants.ACTION_DEVICE_BOOT: {
                AppUpgradeManager.getInstance().upgrade();

                break;
            }
            case BroadCastConstants.ACTION_APP_UPGRADE: {
                String packageName = intent.getStringExtra(Constants.EXTRA_KEY_PACKAGE_NAME);
                AppUpgradeManager.getInstance().upgradeByPackageName(packageName);
                break;
            }
            default:
                break;
        }

        return START_NOT_STICKY;
    }

    private void setForegroundService(){
        Notification notification = new Notification();
        notification.flags = Notification.FLAG_ONGOING_EVENT;
        notification.flags |= Notification.FLAG_NO_CLEAR;
        notification.flags |= Notification.FLAG_FOREGROUND_SERVICE;
        startForeground(1, notification);
    }
}
