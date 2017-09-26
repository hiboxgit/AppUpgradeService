package com.lachesis.appupgradeservice.modules.upgrade.model;

/**
 * Created by boxue.hao on 2017/9/26.
 */

public class UpgradePackageInfo {
    private String softMark;
    private String remoteUrl;
    private String localPath; //完整路径包括文件名

    private boolean isDownloaded;
    private boolean isInstalled; //通过比较版本去掉这个字段
    private int downloadedPackageVersionCode;

    public UpgradePackageInfo() {
    }

    public UpgradePackageInfo(String softMark, String remoteUrl, String localPath) {
        this.softMark = softMark;
        this.remoteUrl = remoteUrl;
        this.localPath = localPath;
    }

    public String getSoftMark() {
        return softMark;
    }

    public void setSoftMark(String softMark) {
        this.softMark = softMark;
    }

    public String getRemoteUrl() {
        return remoteUrl;
    }

    public void setRemoteUrl(String remoteUrl) {
        this.remoteUrl = remoteUrl;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public boolean isDownloaded() {
        return isDownloaded;
    }

    public void setDownloaded(boolean downloaded) {
        isDownloaded = downloaded;
    }

    public boolean isInstalled() {
        return isInstalled;
    }

    public void setInstalled(boolean installed) {
        isInstalled = installed;
    }

    public int getDownloadedPackageVersionCode() {
        return downloadedPackageVersionCode;
    }

    public void setDownloadedPackageVersionCode(int downloadedPackageVersionCode) {
        this.downloadedPackageVersionCode = downloadedPackageVersionCode;
    }
}
