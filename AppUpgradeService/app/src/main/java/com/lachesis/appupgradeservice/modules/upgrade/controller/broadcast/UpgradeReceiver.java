package com.lachesis.appupgradeservice.modules.upgrade.controller.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.lachesis.appupgradeservice.modules.upgrade.controller.service.UpgradeService;

public class UpgradeReceiver extends BroadcastReceiver {

    private static String TAG = "UpgradeReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.

        if (intent == null) {
            return;
        }

        String action = intent.getAction();
        if (action == null) {
            Log.i(TAG,"onReceive action=null, return");
            return;
        }

        Log.i(TAG,"onReceive action="+action);
        startService(context, action);
    }

    public void startService(Context context, String action) {
        Log.i("startService action={}", action);
        Intent intent = new Intent(context, UpgradeService.class);
        intent.setAction(action);
//        intent.putExtra(Constants.UPGRADE_BROADCAST_INTENT_EXTRA_ACTION, action);
        context.startService(intent);
    }
}
