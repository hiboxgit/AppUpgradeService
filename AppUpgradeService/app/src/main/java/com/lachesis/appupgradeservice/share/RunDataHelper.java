package com.lachesis.appupgradeservice.share;

import android.content.Context;

/**
 *  运行状态机类
 *
 * （用于管理全局运行状态变量）
 *
 * Created by Robert on 2017/9/20.
 */

public class RunDataHelper {

    private static RunDataHelper instance = new RunDataHelper();

    /* *
     *
     * 运行状态通用变量
     *
     */

    private RunDataHelper(){

        init();
    }

    public static RunDataHelper getInstance(){

        return instance;
    }

    //初始化状态机参数
    private void init(){


    }

}
