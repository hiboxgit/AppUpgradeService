package com.lachesis.appupgradeservice.modules.upgrade.controller.service;

import android.app.Dialog;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.view.WindowManager;

import com.lachesis.appupgradeservice.modules.upgrade.controller.core.AppUpgradeManager;
import com.lachesis.appupgradeservice.modules.upgrade.model.UpgradeCompleteEvent;
import com.lachesis.appupgradeservice.modules.upgrade.model.UpgradeTipEvent;
import com.lachesis.appupgradeservice.share.BroadCastConstants;
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

        EventBus.getDefault().unregister(this);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
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

        switch (action) {

            case BroadCastConstants.ACTION_DEVICE_BOOT: {


                break;
            }
            case BroadCastConstants.ACTION_UPGRADE_START_UPGRADE: {


                break;
            }
            default:
                break;
        }

        return START_NOT_STICKY;
    }

    @Subscribe
    public void onEventMainThread(UpgradeTipEvent event) {
        Log.i(TAG, "弹框提示有更新...");
        upgradeTipDialog = new SimpleDialog(this)
                .setTitle("发现新版本")
                .setText(event.getNote())
                .setTitleTextColor(0x000000)
                .setLeftBtnTextColor(0xC9CACA)
                .setRightBtnTextColor(0x0000FF)
                .setLeftButton("稍后更新", new SimpleDialog.OnButtonClickListener() {
                    @Override
                    public void onClick(Dialog dialog) {
                        cancelCountTask();
                        upgradeTipDialog.dismiss();

                        AppUpgradeManager.getInstance().delay2ShowUpradeDialog();
                    }
                })
                .setRightButton("立即更新", new SimpleDialog.OnButtonClickListener() {
                    @Override
                    public void onClick(Dialog dialog) {
                        cancelCountTask();
                        startUpdate();
                    }
                });

        upgradeTipDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        upgradeTipDialog.show();

        //开始倒计时任务
        cancelCountTask();
        countTaskSubscription = TaskUtils.countdown(10)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Integer>() {
                    @Override
                    public void onCompleted() {
                        Log.i(TAG,"倒计时完成");
                        cancelCountTask();
                        startUpdate();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.i(TAG,"更新倒计时出错");
                    }

                    @Override
                    public void onNext(Integer integer) {
                        Log.i(TAG,"更新倒计时："+integer);
                        upgradeTipDialog.updateRightButtonText("立即更新("+integer+"s)");
                    }
                });

    }

    @Subscribe
    public void onEventMainThread(UpgradeCompleteEvent event) {
        Log.i(TAG, "弹框提示升级成功...");
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }

        completeTipDialog = new SimpleDialog(this)
                .setTitle("升级成功！");
        completeTipDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        completeTipDialog.show();

        AppUpgradeManager.getInstance().delay2Do(3, new Runnable() {
            @Override
            public void run() {
                completeTipDialog.dismiss();
            }
        });
    }

    public void cancelCountTask(){
        if(countTaskSubscription != null && countTaskSubscription.isUnsubscribed()){
            countTaskSubscription.unsubscribe();
            countTaskSubscription = null;
        }
    }

    public void startUpdate(){
        upgradeTipDialog.dismiss();

//        loadingDialog = new LoadingDialog(UpgradeService.this)
//                .setText("正在更新...")
//                .setTextColor(0x000000);
//        loadingDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
//        loadingDialog.show();
        AppUpgradeManager.getInstance().install();
    }
}
