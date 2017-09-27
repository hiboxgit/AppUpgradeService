package com.lachesis.appupgradeservice.modules.upgrade.view;

import android.app.Application;
import android.app.Dialog;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import com.lachesis.appupgradeservice.R;
import com.lachesis.appupgradeservice.modules.upgrade.controller.core.AppUpgradeManager;
import com.lachesis.appupgradeservice.share.RunDataHelper;
import com.lachesis.common.base.IBaseAsyncHandler;
import com.lachesis.common.ui.dialog.LoadingDialog;
import com.lachesis.common.ui.dialog.SimpleDialog;
import com.lachesis.common.utils.TaskUtils;

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
    Application context;
    private SimpleDialog upgradeTipDialog;
    private LoadingDialog loadingDialog;
    private LoadingDialog completeTipDialog;
    private SimpleDialog serverConfigDialog;

    private Subscription countTaskSubscription;
    private Subscription showRunningSubscription;
    private Subscription showCompleteSubscription;
    private Subscription dismissCompleteSubscription;
    private Subscription delayShowTipSubscription;

    public UpgradeViewModel(Application context) {
        this.context = context;
    }

    public void onShowUpgradeTip(){
        if(upgradeTipDialog != null && upgradeTipDialog.isShowing()){
            upgradeTipDialog.dismiss();
        }
        upgradeTipDialog = new SimpleDialog(context)
                .setTitle("发现新版本")
                .setText("升级内容")
                .setTitleTextColor(Color.parseColor("#FF0000"))
                .setContentTextColor(Color.parseColor("#FFFF0000"))//0xFFFF0000)
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

        upgradeTipDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        upgradeTipDialog.show();

        //开始倒计时任务
        cancelCountTask();
        countTaskSubscription = TaskUtils.countdown(20)
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


    public void onShowLoading(){
        showRunningSubscription = TaskUtils.runMainThread()
                .subscribe(s->{
                    if(loadingDialog != null && loadingDialog.isShowing()){
                        loadingDialog.dismiss();
                    }

                    loadingDialog = new LoadingDialog(context)
                            .setText(" 正在更新...")
                            .setTextColor(Color.parseColor("#000000"));
                    loadingDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                    loadingDialog.show();
                });
    }

    public void onShowComplete(){
        Log.i(TAG,"显示完成对话框.");
        showCompleteSubscription = TaskUtils.runMainThread()
                .subscribe(s->{
                    Log.i(TAG,"开始显示完成对话框.");
                    if(loadingDialog != null && loadingDialog.isShowing()){
                        loadingDialog.dismiss();
                    }
//                    if(completeTipDialog != null && completeTipDialog.isShowing()){
//                        completeTipDialog.dismiss();
//                    }
                    completeTipDialog = new LoadingDialog(context, R.drawable.update_complete)
                            .setText("完成更新!")
                            .setTextColor(Color.parseColor("#000000"));
                    completeTipDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                    completeTipDialog.show();

                    dismissCompleteSubscription = TaskUtils.delay2Do(3)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(s1->{
                                Log.i(TAG,"时间到，完成窗口消失");
                                if(completeTipDialog != null &&completeTipDialog.isShowing()){
                                    completeTipDialog.dismiss();
                                }
                            });
                });
    }

    public void onSetServer(IBaseAsyncHandler callback){
        serverConfigDialog = new SimpleDialog(context)
                .setTitle("升级服务器配置")
                .setInputHint("请输入升级服务器地址")
                .setTitleTextColor(Color.parseColor("#000000"))
                .setInputTextColor(Color.parseColor("#000000"))//0xFFFF0000)
                .setLeftBtnTextColor(Color.parseColor("#000000"))//0xC9CACA)
                .setRightBtnTextColor(Color.parseColor("#000000"))//0x0000FF)
                .setLeftButton("取消", new SimpleDialog.OnButtonClickListener() {
                    @Override
                    public void onClick(Dialog dialog) {
                        serverConfigDialog.dismiss();
                    }
                })
                .setRightButton("确定", new SimpleDialog.OnButtonClickListener() {
                    @Override
                    public void onClick(Dialog dialog) {
                        serverConfigDialog.dismiss();

                        RunDataHelper.getInstance().setServerHostConfig(serverConfigDialog.getInputText());
                        if(callback != null){
                            callback.onSuccess(null);
                        }
                    }
                });

        String host = RunDataHelper.getInstance().getServerHostConfig();
        if(host !=null && !host.equals("")){
            serverConfigDialog.setInputText(host);
        }
        serverConfigDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        serverConfigDialog.show();
    }

    public void cancelCountTask(){
        if(countTaskSubscription != null && !countTaskSubscription.isUnsubscribed()){
            countTaskSubscription.unsubscribe();
            countTaskSubscription = null;
        }
    }

    public void startUpdate(){
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
}