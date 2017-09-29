package com.dudu.upgrade.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.dudu.commonlib.CommonLib;
import com.dudu.commonlib.utils.screen.DensityUtil;
import com.dudu.commonlib.utils.screen.WindowUtils;
import com.dudu.upgrade.R;

/**
 * App更新窗口Manager
 *
 * Created by zhongweiguang on 2017/3/8.
 */

public class UpdateWindowManager {

    @SuppressLint("StaticFieldLeak")
    private static UpdateWindowManager appBindingAlertManager;

    private Context context;
    private View contentView;

    private UpdateWindowManager() {
        context = CommonLib.getInstance().getContext();
    }

    public static UpdateWindowManager getInstance() {
        if (appBindingAlertManager == null) {
            synchronized (UpdateWindowManager.class) {
                if (appBindingAlertManager == null) {
                    appBindingAlertManager = new UpdateWindowManager();
                }
            }
        }

        return appBindingAlertManager;
    }

    private View getContentView() {
        LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(R.layout.layout_update, null);
    }

    private WindowManager.LayoutParams getContentViewLayoutParams() {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
        params.format = PixelFormat.TRANSLUCENT;
        params.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                WindowManager.LayoutParams.FLAG_DIM_BEHIND;

        params.gravity = Gravity.CENTER_VERTICAL;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        params.width = WindowManager.LayoutParams.MATCH_PARENT;

        return params;
    }

    public void show() {
        dismiss();

        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        contentView = getContentView();
        windowManager.addView(contentView, getContentViewLayoutParams());
    }

    public void dismiss() {
        if (contentView != null) {
            WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            windowManager.removeView(contentView);

            contentView = null;
        }
    }

}
