package com.lachesis.appupgradeservice.modules.upgrade.model;

/**
 * Created by boxue.hao on 2017/10/10.
 */

public class ParamSetInfo {
    int seqId;
    String configCode;
    String configValue;
    String configType;
    String configOwner;
    String status;
    String configDesc;
    String createTime;

    public int getSeqId() {
        return seqId;
    }

    public void setSeqId(int seqId) {
        this.seqId = seqId;
    }

    public String getConfigCode() {
        return configCode;
    }

    public void setConfigCode(String configCode) {
        this.configCode = configCode;
    }

    public String getConfigValue() {
        return configValue;
    }

    public void setConfigValue(String configValue) {
        this.configValue = configValue;
    }

    public String getConfigType() {
        return configType;
    }

    public void setConfigType(String configType) {
        this.configType = configType;
    }

    public String getConfigOwner() {
        return configOwner;
    }

    public void setConfigOwner(String configOwner) {
        this.configOwner = configOwner;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getConfigDesc() {
        return configDesc;
    }

    public void setConfigDesc(String configDesc) {
        this.configDesc = configDesc;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "ParamSetInfo{" +
                "seqId=" + seqId +
                ", configCode='" + configCode + '\'' +
                ", configValue='" + configValue + '\'' +
                ", configType='" + configType + '\'' +
                ", configOwner='" + configOwner + '\'' +
                ", status='" + status + '\'' +
                ", configDesc='" + configDesc + '\'' +
                ", createTime='" + createTime + '\'' +
                '}';
    }
}
