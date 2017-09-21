package com.lachesis.appupgradeservice;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.lachesis.appupgradeservice.modules.upgrade.controller.core.AppUpgradeManager;

public class MainActivity extends AppCompatActivity {

    private static String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    public void onStartInstall(View view){

        new Thread(new Runnable() {
            @Override
            public void run() {
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
        }).start();
    }


}
