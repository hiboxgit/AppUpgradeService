package com.lachesis.common.base;

import java.util.Objects;

/**
 *
 * 异步任务回调处理
 *
 * Created by Robert on 2017/9/20.
 */

public interface IBaseAsyncHandler {

    //成功回调接口
    void onSuccess(Object result);

    //失败回调接口
    void onError(Object result);

    //成功和失败都会回调的接口
    void onComplete(Object result);
}
