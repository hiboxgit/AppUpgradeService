package com.lachesis.common;

import android.app.Application;
import android.content.Context;

import com.lachesis.common.utils.Utils;
import com.lzy.okgo.OkGo;

/**
 * Created by Robert on 2017/9/21.
 */

public class CommonLib {

    private static CommonLib instance = new CommonLib();
    private Application context;

    public static CommonLib getInstance(){
        return instance;
    }

    private CommonLib(){

    }

    public void init(Application application){
        this.context = application;

        //初始化工具类
        Utils.init(context);

        //初始化okgo网络框架
        OkGo.getInstance().init(application);
    }

    public Application getContext(){
        return context;
    }
}