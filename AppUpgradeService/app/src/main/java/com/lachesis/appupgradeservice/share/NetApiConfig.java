package com.lachesis.appupgradeservice.share;

/**
 * 网络api配置类
 * Created by Robert on 2017/9/20.
 */

public class NetApiConfig {

//    public static String SERVER_HOST = "http://10.2.1.87:9099/";
    public static final String UPDATE_CHECK = "/windranger/ldm/SoftUpgradeManages/getUpgradeSoft";
    public static final String UPGRADE_INSTALL_DELAY = "/windranger/sys/SysConfigs/softdelayTime"; //立即更新的延迟时间设置
}
