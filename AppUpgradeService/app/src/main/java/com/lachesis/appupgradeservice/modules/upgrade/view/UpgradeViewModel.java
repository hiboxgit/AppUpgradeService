package com.lachesis.appupgradeservice.modules.upgrade.view;

import android.app.Application;
import android.app.Dialog;
import android.graphics.Color;
import android.view.WindowManager;

import com.lachesis.appupgradeservice.R;
import com.lachesis.appupgradeservice.modules.upgrade.controller.core.AppUpgradeManager;
import com.lachesis.appupgradeservice.share.RunDataHelper;
import com.lachesis.common.base.IBaseAsyncHandler;
import com.lachesis.common.ui.dialog.LoadingDialog;
import com.lachesis.common.ui.dialog.SimpleDialog;
import com.lachesis.common.utils.TaskUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by boxue.hao on 2017/9/27.
 */

public class UpgradeViewModel {

    private static String TAG = "UpgradeViewModel";
    private Logger logger = LoggerFactory.getLogger("LxAppUpgrade");
    private static final int DELAY_COUNT = 10; //倒计时时长

    Application context;
    private SimpleDialog upgradeTipDialog;
    private LoadingDialog loadingDialog;
    private LoadingDialog completeTipDialog;
    private SimpleDialog serverConfigDialog;
    private SimpleDialog noUpgradeTipDialog;

    private Subscription countTaskSubscription;
    private Subscription showRunningSubscription;
    private Subscription showCompleteSubscription;
    private Subscription dismissCompleteSubscription;
    private Subscription delayShowTipSubscription;
    private Subscription startUpgradeTipSubscription;
    private Subscription hostCheckTaskSubscription;
    private Subscription hostReCheckTaskSubscription;
    private Subscription noUpdateTipSubscription;

    public UpgradeViewModel(Application context) {
        this.context = context;
    }

    public void onShowUpgradeTip() {
        startUpgradeTipSubscription = TaskUtils.runMainThread()
                .subscribe(s -> {
                    if (upgradeTipDialog != null && upgradeTipDialog.isShowing()) {
                        upgradeTipDialog.dismiss();
                    }
                    upgradeTipDialog = new SimpleDialog(context)
                            .setTitle("发现新版本")
                            .setText(AppUpgradeManager.getInstance().updateNote)
                            .setTitleTextColor(Color.parseColor("#000000"))
                            .setContentTextColor(Color.parseColor("#000000"))//0xFFFF0000)
                            .setLeftBtnTextColor(Color.parseColor("#C9CACA"))//0xC9CACA)
                            .setRightBtnTextColor(Color.parseColor("#0000FF"))//0x0000FF)
                            .setLeftButton("稍后更新", new SimpleDialog.OnButtonClickListener() {
                                @Override
                                public void onClick(Dialog dialog) {
                                    upgradeTipDialog.dismiss();
                                    cancelCountTask();
                                    delay2ShowUpradeDialog();
                                }
                            })
                            .setRightButton("立即更新", new SimpleDialog.OnButtonClickListener() {
                                @Override
                                public void onClick(Dialog dialog) {
                                    upgradeTipDialog.dismiss();
                                    cancelCountTask();
                                    startUpdate();
                                }
                            });

                    upgradeTipDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_TOAST);
                    upgradeTipDialog.show();

                    //开始倒计时任务
                    cancelCountTask();
                    countTaskSubscription = TaskUtils.countdown(DELAY_COUNT)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Subscriber<Integer>() {
                                @Override
                                public void onCompleted() {
                                    logger.info("倒计时完成");
                                    cancelCountTask();
                                    startUpdate();
                                }

                                @Override
                                public void onError(Throwable e) {
                                    logger.info("更新倒计时出错");
                                }

                                @Override
                                public void onNext(Integer integer) {
                                    logger.info("更新倒计时：" + integer);
                                    upgradeTipDialog.updateRightButtonText("立即更新(" + integer + "s)");
                                }
                            });
                });
    }


    public void onShowLoading() {
        showRunningSubscription = TaskUtils.runMainThread()
                .subscribe(s -> {
                    if (loadingDialog != null && loadingDialog.isShowing()) {
                        loadingDialog.dismiss();
                    }

                    loadingDialog = new LoadingDialog(context)
                            .setText(" 正在更新...")
                            .setTextColor(Color.parseColor("#000000"));
                    loadingDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_TOAST);
                    loadingDialog.show();
                });
    }

    public void onShowComplete() {
        logger.info("显示完成对话框.");
        showCompleteSubscription = TaskUtils.runMainThread()
                .subscribe(s -> {
                    logger.info("开始显示完成对话框.");
                    if (loadingDialog != null && loadingDialog.isShowing()) {
                        loadingDialog.dismiss();
                    }
                    completeTipDialog = new LoadingDialog(context, R.drawable.update_complete)
                            .setText("完成更新!")
                            .setTextColor(Color.parseColor("#000000"));
                    completeTipDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_TOAST);
                    completeTipDialog.show();

                    dismissCompleteSubscription = TaskUtils.delay2Do(3)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(s1 -> {
                                logger.info("时间到，完成窗口消失");
                                if (completeTipDialog != null && completeTipDialog.isShowing()) {
                                    completeTipDialog.dismiss();
                                }

                                AppUpgradeManager.getInstance().onUpgradeStopRun();
                            });
                });
    }

    public void onSetServer(IBaseAsyncHandler callback) {
        hostCheckTaskSubscription = TaskUtils.runMainThread()
                .subscribe(s -> {
                    serverConfigDialog = new SimpleDialog(context)
                            .setTitle("升级服务器配置")
                            .setInputHint("请输入升级服务器地址")
                            .setTitleTextColor(Color.parseColor("#000000"))
                            .setInputTextColor(Color.parseColor("#000000"))
                            .setLeftBtnTextColor(Color.parseColor("#000000"))
                            .setRightBtnTextColor(Color.parseColor("#000000"))
                            .setLeftButton("取消", new SimpleDialog.OnButtonClickListener() {
                                @Override
                                public void onClick(Dialog dialog) {
                                    serverConfigDialog.dismiss();
                                    if (callback != null) {
                                        callback.onError(null);
                                    }
                                }
                            })
                            .setRightButton("确定", new SimpleDialog.OnButtonClickListener() {
                                @Override
                                public void onClick(Dialog dialog) {
                                    serverConfigDialog.dismiss();

                                    RunDataHelper.getInstance().setServerHostConfig(serverConfigDialog.getInputText());
                                    if (callback != null) {
                                        callback.onSuccess(null);
                                    }
                                }
                            });

                    String host = RunDataHelper.getInstance().getServerHostConfig();
                    if (host != null && !host.equals("")) {
                        serverConfigDialog.setInputText(host);
                    }

                    serverConfigDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE|WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE); //防止软键盘遮挡输入框
                    serverConfigDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_TOAST);
                    serverConfigDialog.show();

                });
    }

    public void onFailSetServer(IBaseAsyncHandler callback) {
        hostReCheckTaskSubscription = TaskUtils.runMainThread()
                .subscribe(s -> {
                    serverConfigDialog = new SimpleDialog(context)
                            .setTitle("升级请求失败")
                            .setInputHint("请输入升级服务器地址")
                            .setTitleTextColor(Color.parseColor("#FF0000"))
                            .setInputTextColor(Color.parseColor("#000000"))
                            .setLeftBtnTextColor(Color.parseColor("#000000"))
                            .setRightBtnTextColor(Color.parseColor("#000000"))
                            .setLeftButton("取消", new SimpleDialog.OnButtonClickListener() {
                                @Override
                                public void onClick(Dialog dialog) {
                                    serverConfigDialog.dismiss();
                                    if (callback != null) {
                                        callback.onError(null);
                                    }
                                }
                            })
                            .setRightButton("确定", new SimpleDialog.OnButtonClickListener() {
                                @Override
                                public void onClick(Dialog dialog) {
                                    serverConfigDialog.dismiss();

                                    RunDataHelper.getInstance().setServerHostConfig(serverConfigDialog.getInputText());
                                    if (callback != null) {
                                        callback.onSuccess(null);
                                    }
                                }
                            });

                    String host = RunDataHelper.getInstance().getServerHostConfig();
                    if (host != null) {
                        serverConfigDialog.setInputText(host);
                    }

                    serverConfigDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
                    serverConfigDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_TOAST);
                    serverConfigDialog.show();
                });
    }

    public void cancelCountTask() {
        if (countTaskSubscription != null && !countTaskSubscription.isUnsubscribed()) {
            countTaskSubscription.unsubscribe();
            countTaskSubscription = null;
        }
    }

    public void startUpdate() {
        upgradeTipDialog.dismiss();
        AppUpgradeManager.getInstance().install();
    }

    public void delay2ShowUpradeDialog() {
        delayShowTipSubscription = Observable.timer(10, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    if (delayShowTipSubscription != null && !delayShowTipSubscription.isUnsubscribed()) {
                        delayShowTipSubscription.unsubscribe();
                        delayShowTipSubscription = null;
                    }
                    onShowUpgradeTip();
                });
    }

    public void onShowNoUpgradeTip() {
        noUpdateTipSubscription = TaskUtils.runMainThread()
                .subscribe(s -> {
                    if (noUpgradeTipDialog != null && noUpgradeTipDialog.isShowing()) {
                        noUpgradeTipDialog.dismiss();
                    }
                    noUpgradeTipDialog = new SimpleDialog(context)
                            .setTitle("当前为最新版本")
                            .setTitleTextColor(Color.parseColor("#000000"))
                            .setLeftBtnTextColor(Color.parseColor("#000000"))
                            .setLeftButton("确定", new SimpleDialog.OnButtonClickListener() {
                                @Override
                                public void onClick(Dialog dialog) {
                                    noUpgradeTipDialog.dismiss();
                                }
                            });

                    noUpgradeTipDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_TOAST);
                    noUpgradeTipDialog.show();
                });
    }

}
