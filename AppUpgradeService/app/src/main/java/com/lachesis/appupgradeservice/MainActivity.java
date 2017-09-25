package com.lachesis.appupgradeservice;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.lachesis.appupgradeservice.modules.upgrade.controller.core.AppUpgradeManager;
import com.lachesis.appupgradeservice.modules.upgrade.controller.service.UpgradeService;
import com.lachesis.appupgradeservice.share.NetApiConfig;

public class MainActivity extends AppCompatActivity {

    private static String TAG = "MainActivity";

    private EditText inputUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inputUrl = (EditText)this.findViewById(R.id.url_edit);
    }


    public void onStartInstall(View view){

//        String url = inputUrl.getText().toString();

//        if(url != null && url.startsWith("http://")){
//            NetApiConfig.SERVER_HOST = url;
//        }

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




}
