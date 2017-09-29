package com.dudu.upgrade.fota;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

/**
 * Created by raise.yang on 2016/07/15.
 */
public class LocalReceiver extends BroadcastReceiver {
    //广播类型
    public static final String ACTION_DOWNLOAD_PROGRESS = "action.iport.download.progress";
    public static final String ACTION_DOWNLOAD_ERROR = "action.iport.download.error";
    public static final String ACTION_DOWNLOAD_FINISHED = "action.iport.download.finished";

    public static final String ACTION_CHECK_START = "action.check.start";
    public static final String ACTION_CHECK_SUCCESS = "action.check.success";
    public static final String ACTION_CHECK_ERROR = "action.check.error";

    public static final String ACTION_UPGRADE_START = "action.upgrade.start";
    public static final String ACTION_UPGRADE_ERROR = "action.upgrade.error";

    //传值的key
    public static final String KEY_INTENT_ARG0 = "key_intent_progress";
    //主界面对象
    private FotaContract.View m_view;

    public LocalReceiver(FotaContract.View view) {
        m_view = view;
    }

    public static void sendReceiver(Context context, String action, String arg0) {
        Intent intent = new Intent(action);
        if (arg0 != null) {
            intent.putExtra(KEY_INTENT_ARG0, arg0);
        }
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) return;
        String action = intent.getAction();
        switch (action) {
            case ACTION_CHECK_START:
                m_view.show_checking();
                break;
            case ACTION_CHECK_SUCCESS:
                m_view.show_can_download();
                break;
            case ACTION_DOWNLOAD_PROGRESS:
                m_view.show_downloading(Integer.parseInt(intent.getStringExtra(KEY_INTENT_ARG0)));
                break;
            case ACTION_DOWNLOAD_FINISHED:
                m_view.show_can_upgrade();
                break;
            case ACTION_UPGRADE_START:
                m_view.show_upgrading();
                break;
            case ACTION_CHECK_ERROR:
            case ACTION_DOWNLOAD_ERROR:
            case ACTION_UPGRADE_ERROR:
                m_view.show_error(intent.getStringExtra(KEY_INTENT_ARG0));
                break;

        }
    }
}
