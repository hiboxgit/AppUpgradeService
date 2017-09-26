package com.lachesis.common.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.facebook.common.util.UriUtil;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeControllerBuilder;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.animated.base.AbstractAnimatedDrawable;
import com.facebook.imagepipeline.image.ImageInfo;
import com.lachesis.common.R;

import java.lang.reflect.Field;


/**
 * 加载对话框
 * <p>
 * Created by zhongweiguang on 2016/12/8.
 */

public class LoadingDialog extends Dialog {

    private TextView textTv;
    private SimpleDraweeView simpleDraweeView;

    public LoadingDialog(Context context) {
        super(context, R.style.SimpleDialog);
        initView(context);
    }

    public LoadingDialog(Context context, int resId) {
        super(context, R.style.SimpleDialog);
        initView(context,resId);
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

        Log.i("LoadingDialog对话框尺寸","layoutParams.width:"+layoutParams.width+",layoutParams.height:"+layoutParams.height);
    }

    private void initView(Context context) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.loading_dialog, null);

        setContentView(view);
        setCanceledOnTouchOutside(false);
        setCancelable(false);

        textTv = (TextView) findViewById(R.id.textView);
        simpleDraweeView = (SimpleDraweeView) findViewById(R.id.simpleDraweeView);

//        DraweeController draweeController =
//                Fresco.newDraweeControllerBuilder()
//                        .setUri("asset:///loading/loading.gif")
//                        .setOldController(simpleDraweeView.getController())
//                        .setAutoPlayAnimations(true)
//                        .build();
//        simpleDraweeView.setController(draweeController);


        ControllerListener controllerListener = new BaseControllerListener<ImageInfo>() {
            @Override
            public void onFinalImageSet(
                    String id,
                    @Nullable ImageInfo imageInfo,
                    @Nullable Animatable anim) {
                if (anim != null) {
                    // 其他控制逻辑
                    Log.i("","开始播放gif");
                    anim.start();
                }
            }

        };


        Uri uri = new Uri.Builder()
                .scheme(UriUtil.LOCAL_RESOURCE_SCHEME)
                .path(String.valueOf(R.drawable.loading))
                .build();

        DraweeController draweeController = Fresco.newDraweeControllerBuilder()
                .setAutoPlayAnimations(true)
                .setOldController(simpleDraweeView.getController())
//                .setControllerListener(controllerListener)
//                .setUri(uri)//设置uri
//                .setUri(Uri.parse("http://img.huofar.com/data/jiankangrenwu/shizi.gif"))
                .setUri("asset:///loading/loading.gif")
                .build();

        simpleDraweeView.setController(draweeController);


//        PipelineDraweeControllerBuilder pipelineDraweeControllerBuilder = Fresco.newDraweeControllerBuilder().setAutoPlayAnimations(false);
//        DraweeController draweeController = pipelineDraweeControllerBuilder.setUri(uri)
//                .setControllerListener(controllerListener)
//                .setAutoPlayAnimations(true)
//                .build();
//        simpleDraweeView.setController(draweeController);

    }

    private void initView(Context context, int resId) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.loading_dialog, null);

        setContentView(view);
        setCanceledOnTouchOutside(false);
        setCancelable(false);

        textTv = (TextView) findViewById(R.id.textView);
        simpleDraweeView = (SimpleDraweeView) findViewById(R.id.simpleDraweeView);
        simpleDraweeView.setImageResource(resId);
    }

    public LoadingDialog setImage(int resId) {
        simpleDraweeView.setImageResource(resId);
        return this;
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

    public LoadingDialog setTextColor(int color) {
        textTv.setTextColor(color);
        return this;
    }
}
