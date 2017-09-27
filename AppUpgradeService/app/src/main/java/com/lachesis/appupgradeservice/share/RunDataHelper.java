package com.lachesis.appupgradeservice.share;

import android.content.Context;
import android.util.Log;

import com.lachesis.common.ui.dialog.LoadingDialog;
import com.lachesis.common.utils.SPUtils;

/**
 *  运行状态机类
 *
 * （用于管理全局运行状态变量）
 *
 * Created by Robert on 2017/9/20.
 */

public class RunDataHelper {

    private static RunDataHelper instance = new RunDataHelper();

    private static final String TAG = "LxUpgrade";
    /* *
     *
     * 运行状态通用变量
     *
     */

    private SPUtils spAgent;
    private String serverHostConfig;

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
        if(!serverHostConfig.endsWith("/")){
            serverHostConfig = serverHostConfig+"/";
        }
        this.serverHostConfig = serverHostConfig;
        spAgent.putString(Constants.SP_KEY_UPGRADE_SERVER_HOST,serverHostConfig);
    }

    public boolean hasValidServerHost(){

        if(serverHostConfig!=null){
            Log.i(TAG,"serverHostConfig :"+serverHostConfig);
        }else{
            Log.i(TAG,"serverHostConfig = null");
        }

        if(serverHostConfig.isEmpty()){
            Log.i(TAG,"serverHostConfig isEmpty");
        }

        if(!serverHostConfig.startsWith("http://")){
            Log.i(TAG,"serverHostConfig not start http://");
        }
        if(!serverHostConfig.endsWith("/")){
            Log.i(TAG,"serverHostConfig not end /");
        }

        if(serverHostConfig!=null &&
                !serverHostConfig.isEmpty() &&
                serverHostConfig.startsWith("http://") &&
                serverHostConfig.endsWith("/")){
            return true;
        }
        return false;
    }
}
