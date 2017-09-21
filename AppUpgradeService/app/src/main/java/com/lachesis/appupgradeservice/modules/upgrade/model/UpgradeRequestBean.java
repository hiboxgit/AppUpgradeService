package com.lachesis.appupgradeservice.modules.upgrade.model;

/**
 * Created by Robert on 2017/9/21.
 */

public class UpgradeRequestBean {

    private String softMark;
    private int softVersion;

    public void setSoftMark(String softMark){
        this.softMark = softMark;
    }
    public String getSoftMark(){
        return this.softMark;
    }
    public void setSoftVersion(int softVersion){
        this.softVersion = softVersion;
    }
    public int getSoftVersion(){
        return this.softVersion;
    }
}
