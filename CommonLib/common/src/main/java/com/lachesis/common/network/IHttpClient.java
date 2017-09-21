package com.lachesis.common.network;

import com.lachesis.common.base.IBaseAsyncHandler;
import com.lachesis.common.base.IBaseProgressHandler;

import java.util.HashMap;

/**
 *
 * http 客户端接口
 *
 * Created by Robert on 2017/9/21.
 */

public interface IHttpClient {

    //get请求
    void get(String url, //请求URL
             HashMap<String, String> params, //请求参数
             IBaseAsyncHandler handler); //结果回调

    //post方法
    void post(String url, //请求URL
              HashMap<String, String> params, //请求参数
              IBaseAsyncHandler handler); //结果回调

    //上传文件
    void upload(String url, //请求URL
                HashMap<String, String> params, //请求参数
                String localFilePath, //本地文件路径
                IBaseProgressHandler progressHandler, //进度回调
                IBaseAsyncHandler handler); //结果回调

    //下载文件
    void download(String url, //请求URL
                  HashMap<String, String> params, //请求参数
                  String localFilePath, //本地文件路径
                  IBaseProgressHandler progressHandler, //进度回调
                  IBaseAsyncHandler handler); //结果回调
}
