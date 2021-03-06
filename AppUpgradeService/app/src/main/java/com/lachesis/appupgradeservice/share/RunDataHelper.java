package com.lachesis.appupgradeservice.share;

import android.content.Context;
import android.util.Log;

import com.lachesis.common.ui.dialog.LoadingDialog;
import com.lachesis.common.utils.SPUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  运行状态机类
 *
 * （用于管理全局运行状态变量）
 *
 * Created by Robert on 2017/9/20.
 */

public class RunDataHelper {
    private static final String TAG = "LxUpgrade";
    private Logger logger = LoggerFactory.getLogger("LxAppUpgrade");
    private static RunDataHelper instance = new RunDataHelper();

    /* *
     *
     * 运行状态通用变量
     *
     */

    private SPUtils spAgent;
    private String serverHostConfig;
    private int delayInstallTime;
    private int checkInterval;

    private RunDataHelper(){

    }

    public static RunDataHelper getInstance(){

        return instance;
    }

    //初始化状态机参数
    public void init(){

        if(spAgent == null){
            spAgent = new SPUtils(TAG);
        }

        initParam();
    }

    private void initParam(){
        serverHostConfig = spAgent.getString(Constants.SP_KEY_UPGRADE_SERVER_HOST,"");
        delayInstallTime = spAgent.getInt(Constants.SP_KEY_UPGRADE_DELAY_TIME,10); //
        checkInterval = spAgent.getInt(Constants.SP_KEY_UPGRADE_CHECK_INTERVAL,60);
    }

    public SPUtils getSpAgent() {
        return spAgent;
    }

    public void setSpAgent(SPUtils spAgent) {
        this.spAgent = spAgent;
    }

    public String getServerHostConfig() {
        return serverHostConfig;
    }

    public void setServerHostConfig(String serverHostConfig) {
        if(serverHostConfig!=null &&
                !serverHostConfig.isEmpty() &&
                serverHostConfig.startsWith("http://") &&
                !serverHostConfig.endsWith("/")){
            serverHostConfig = serverHostConfig+"/";
        }

        this.serverHostConfig = serverHostConfig;
        spAgent.putString(Constants.SP_KEY_UPGRADE_SERVER_HOST,serverHostConfig);
    }

    public int getDelayInstallTime() {
        return delayInstallTime;
    }

    public void setDelayInstallTime(int delayInstallTime) {
        this.delayInstallTime = delayInstallTime;
        spAgent.putInt(Constants.SP_KEY_UPGRADE_DELAY_TIME,this.delayInstallTime);
    }

    public boolean hasValidServerHost(){

        if(serverHostConfig!=null){
            logger.info("serverHostConfig :"+serverHostConfig);
        }else{
            logger.info("serverHostConfig = null");
        }

        if(serverHostConfig.isEmpty()){
            logger.info("serverHostConfig isEmpty");
        }

        if(!serverHostConfig.startsWith("http://")){
            logger.info("serverHostConfig not start http://");
        }
        if(!serverHostConfig.endsWith("/")){
            logger.info("serverHostConfig not end /");
        }

        if(serverHostConfig!=null &&
                !serverHostConfig.isEmpty() &&
                serverHostConfig.startsWith("http://") &&
                serverHostConfig.endsWith("/")){
            return true;
        }
        return false;
    }

    public int getCheckInterval() {
        return checkInterval;
    }

    public void setCheckInterval(int checkInterval) {
        this.checkInterval = checkInterval;
        spAgent.putInt(Constants.SP_KEY_UPGRADE_CHECK_INTERVAL,this.checkInterval);
    }
}
