package com.dudu.upgrade;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpgradeReceiver extends BroadcastReceiver {

    private static Logger logger = LoggerFactory.getLogger("dudu_upgrade.UpgradeReceiver");

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) {
            return;
        }

        String action = intent.getAction();
        if (action == null) {
            logger.debug("onReceive action=null, return");
            return;
        }

        logger.debug("onReceive action={}", action);
        UpgradeService.startService(context, action);
    }
}
