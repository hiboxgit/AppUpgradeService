package com.lachesis.appupgradeservice.modules.upgrade.controller.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.lachesis.appupgradeservice.share.BroadCastConstants;

/**
 * app升级管理类
 *
 * Created by Robert on 2017/9/19.
 */
public class UpgradeService extends Service {

    private static String TAG = "UpgradeService";

    public UpgradeService() {
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
            Log.i(TAG,"onStartCommand action=null, return");
            return START_NOT_STICKY;
        }

        switch (action) {

            case BroadCastConstants.ACTION_DEVICE_BOOT:{


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
}
