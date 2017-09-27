package com.lachesis.common;

import android.app.Application;
import android.content.Context;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.lachesis.common.utils.AppUtils;
import com.lachesis.common.utils.Utils;
import com.liulishuo.filedownloader.FileDownloader;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by Robert on 2017/9/21.
 */

public class CommonLib {

    private static CommonLib instance = new CommonLib();
    private Application context;

    public static CommonLib getInstance() {
        return instance;
    }

    private CommonLib() {

    }

    public void init(Application application) {
        this.context = application;

        //初始化工具类
        Utils.init(context);

        //初始化FileDownloader 文件下载框架
        FileDownloader.setupOnApplicationOnCreate(context);

        //初始化数据库框架
        Realm.init(context);
        RealmConfiguration config = new  RealmConfiguration.Builder()
                .name(AppUtils.getAppName(context)+".realm")
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(config);

        //fresco图片框架初始化
        Fresco.initialize(context);
    }

    public Application getContext() {
        return context;
    }

}

