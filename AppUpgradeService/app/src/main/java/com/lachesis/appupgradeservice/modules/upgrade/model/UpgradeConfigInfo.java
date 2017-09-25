package com.lachesis.appupgradeservice.modules.upgrade.model;

/**
 * Created by boxue.hao on 2017/9/25.
 */

public class UpgradeConfigInfo {
    private String localPath;

    public UpgradeConfigInfo(String localPath) {
        this.localPath = localPath;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

}
