package com.lachesis.appupgradeservice;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.lachesis.appupgradeservice.modules.upgrade.controller.core.AppUpgradeManager;
import com.lachesis.appupgradeservice.modules.upgrade.controller.service.UpgradeService;
import com.lachesis.appupgradeservice.share.Constants;
import com.lachesis.appupgradeservice.share.NetApiConfig;
import com.lachesis.appupgradeservice.share.RunDataHelper;
import com.lachesis.common.ui.dialog.LoadingDialog;
import com.lachesis.common.ui.dialog.SimpleDialog;
import com.lachesis.common.utils.SPUtils;
import com.lachesis.common.utils.ScreenUtils;

public class MainActivity extends AppCompatActivity {

    private static String TAG = "MainActivity";

    private EditText inputUrl;
    private SimpleDialog upgradeTipDialog;
    private LoadingDialog loadingDialog;
    private LoadingDialog completeTipDialog;
    private SimpleDialog serverConfigDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inputUrl = (EditText)this.findViewById(R.id.url_edit);

        int width = ScreenUtils.getScreenWidth();
        int height = ScreenUtils.getScreenHeight();
        Log.i("ScreenUtils","width:"+width+",height:"+height);
    }


    public void onStartInstall(View view){

        String url = inputUrl.getText().toString();

        if(url != null && url.startsWith("http://")){
            NetApiConfig.SERVER_HOST = url;
        }

        Log.i(TAG,"start UpgradeService ...}");
        Intent intent = new Intent(this, UpgradeService.class);
        this.startService(intent);


            AppUpgradeManager.getInstance().upgrade(new AppUpgradeManager.UpgradeCallBack() {
                @Override
                public void onComplete() {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast toast=Toast.makeText(getApplicationContext(), "升级成功", Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    });
                }

                @Override
                public void onError() {

                }
            });
    }


    public void onShowUpgradeTip(View view){
        if(upgradeTipDialog != null && upgradeTipDialog.isShowing()){
            upgradeTipDialog.dismiss();
        }
        upgradeTipDialog = new SimpleDialog(this)
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
                    }
                })
                .setRightButton("立即更新", new SimpleDialog.OnButtonClickListener() {
                    @Override
                    public void onClick(Dialog dialog) {
                        upgradeTipDialog.dismiss();
                    }
                });

//        upgradeTipDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        upgradeTipDialog.show();
    }


    public void onShowLoading(View view){
        if(loadingDialog != null && loadingDialog.isShowing()){
            loadingDialog.dismiss();
        }

        loadingDialog = new LoadingDialog(this)
                .setText(" 正在更新...")
                .setTextColor(Color.parseColor("#000000"));
//        loadingDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        loadingDialog.show();
    }

    public void onShowComplete(View view){
        completeTipDialog = new LoadingDialog(this,R.drawable.update_complete)
                .setText("完成更新!")
                .setTextColor(Color.parseColor("#000000"));
//        completeTipDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        completeTipDialog.show();
    }

    public void onSetServer(View view){


        serverConfigDialog = new SimpleDialog(this)
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
                            }
                        });

        String host = RunDataHelper.getInstance().getServerHostConfig();
        if(host !=null && !host.equals(""))
        serverConfigDialog.setInputText(host);

//        upgradeTipDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        serverConfigDialog.show();
    }
}
