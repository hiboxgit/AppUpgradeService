package com.lachesis.common.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.lachesis.common.R;


/**
 * 加载对话框
 *
 * Created by zhongweiguang on 2016/12/8.
 */

public class LoadingDialog  extends Dialog {

    private TextView textTv;

    public LoadingDialog(Context context) {
        super(context, R.style.SimpleDialog);
        initView(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int width = getContext().getResources().getDimensionPixelOffset(R.dimen.loading_dialog_width);
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        layoutParams.width = width;
        layoutParams.y = -getContext().getResources().getDimensionPixelOffset(R.dimen.status_bar_height) / 2;
        getWindow().setGravity(Gravity.CENTER);
        getWindow().setAttributes(layoutParams);
    }

    private void initView(Context context) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.loading_dialog, null);

        setContentView(view);
        setCanceledOnTouchOutside(false);
        setCancelable(false);

        textTv = (TextView) findViewById(R.id.textView);
        SimpleDraweeView simpleDraweeView = (SimpleDraweeView) findViewById(R.id.simpleDraweeView);

        DraweeController draweeController =
                Fresco.newDraweeControllerBuilder()
                        .setUri("asset:///loading/loading.gif")
                        .setOldController(simpleDraweeView.getController())
                        .setAutoPlayAnimations(true)
                        .build();
        simpleDraweeView.setController(draweeController);
    }

    public LoadingDialog setText(String text) {
        textTv.setText(text);
        textTv.setVisibility(View.VISIBLE);
        return this;
    }

    public LoadingDialog setTextRes(@StringRes int resId) {
        textTv.setText(resId);
        textTv.setVisibility(View.VISIBLE);
        return this;
    }
}
