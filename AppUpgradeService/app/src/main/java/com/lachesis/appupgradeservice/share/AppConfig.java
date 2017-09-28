package com.lachesis.appupgradeservice.share;

import com.lachesis.common.utils.SDCardUtils;

/**
 * Created by boxue.hao on 2017/9/22.
 */

public class AppConfig {
    /* 升级包下载路径 */
    public static String APK_DOWNLOAD_DIR = SDCardUtils.getSDCardPath() + "ApkFiles/";
}
