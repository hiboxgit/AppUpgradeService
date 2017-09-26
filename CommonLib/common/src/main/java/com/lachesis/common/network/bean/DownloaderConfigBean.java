package com.lachesis.common.network.bean;

/**
 * Created by boxue.hao on 2017/9/26.
 */

public class DownloaderConfigBean {
    private String remotePath;
    private String localPath;
    private String tag;

    public DownloaderConfigBean(String remotePath, String localPath, String tag) {
        this.remotePath = remotePath;
        this.localPath = localPath;
        this.tag = tag;
    }

    public String getRemotePath() {
        return remotePath;
    }

    public void setRemotePath(String remotePath) {
        this.remotePath = remotePath;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}
