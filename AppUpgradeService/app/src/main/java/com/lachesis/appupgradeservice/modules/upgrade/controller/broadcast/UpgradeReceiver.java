package com.lachesis.appupgradeservice.modules.upgrade.controller.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.lachesis.appupgradeservice.modules.upgrade.controller.service.UpgradeService;
import com.lachesis.appupgradeservice.share.BroadCastConstants;
import com.lachesis.appupgradeservice.share.Constants;

public class UpgradeReceiver extends BroadcastReceiver {

    private static String TAG = "UpgradeReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        Log.i(TAG,"onReceive action="+intent.getAction());


        Intent serviceIntent = new Intent(context, UpgradeService.class);
        serviceIntent.setAction(intent.getAction());
        if(intent.getAction().equals(BroadCastConstants.ACTION_APP_UPGRADE)){
            serviceIntent.putExtra(Constants.EXTRA_KEY_PACKAGE_NAME,intent.getStringExtra(Constants.EXTRA_KEY_PACKAGE_NAME));
        }
        context.startService(serviceIntent);
    }
}
