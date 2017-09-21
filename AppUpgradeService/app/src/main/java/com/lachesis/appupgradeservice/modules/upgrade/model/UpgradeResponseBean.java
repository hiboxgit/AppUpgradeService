package com.lachesis.appupgradeservice.modules.upgrade.model;

/**
 * Created by Robert on 2017/9/21.
 */

public class UpgradeResponseBean {

    private String createPerson;
    private String createTime;
    private Object seqId;
    private String softDesc;
    private String softMark;
    private String softName;
    private String softPath;
    private int softVersion;
    private String status;
    private String updatePerson;
    private String updateTime;
    private String upgradeType;
    private String versionName;

    public void setCreatePerson(String createPerson){
        this.createPerson = createPerson;
    }
    public String getCreatePerson(){
        return this.createPerson;
    }
    public void setCreateTime(String createTime){
        this.createTime = createTime;
    }
    public String getCreateTime(){
        return this.createTime;
    }
    public void setSeqId(Object seqId){
        this.seqId = seqId;
    }
    public Object getSeqId(){
        return this.seqId;
    }
    public void setSoftDesc(String softDesc){
        this.softDesc = softDesc;
    }
    public String getSoftDesc(){
        return this.softDesc;
    }
    public void setSoftMark(String softMark){
        this.softMark = softMark;
    }
    public String getSoftMark(){
        return this.softMark;
    }
    public void setSoftName(String softName){
        this.softName = softName;
    }
    public String getSoftName(){
        return this.softName;
    }
    public void setSoftPath(String softPath){
        this.softPath = softPath;
    }
    public String getSoftPath(){
        return this.softPath;
    }
    public void setSoftVersion(int softVersion){
        this.softVersion = softVersion;
    }
    public int getSoftVersion(){
        return this.softVersion;
    }
    public void setStatus(String status){
        this.status = status;
    }
    public String getStatus(){
        return this.status;
    }
    public void setUpdatePerson(String updatePerson){
        this.updatePerson = updatePerson;
    }
    public String getUpdatePerson(){
        return this.updatePerson;
    }
    public void setUpdateTime(String updateTime){
        this.updateTime = updateTime;
    }
    public String getUpdateTime(){
        return this.updateTime;
    }
    public void setUpgradeType(String upgradeType){
        this.upgradeType = upgradeType;
    }
    public String getUpgradeType(){
        return this.upgradeType;
    }
    public void setVersionName(String versionName){
        this.versionName = versionName;
    }
    public String getVersionName(){
        return this.versionName;
    }

}
