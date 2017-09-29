package com.dudu.upgrade.fota.update;

import android.text.TextUtils;

import com.dudu.upgrade.UpgradeApplication;
import com.fota.iport.SPFTool;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * Created by raise.yang on 2016/08/01.
 */
public class UpdateInfo {


    private static final String KEY_UPDATEINFO_DATA = "key_updateinfo_data";
    /**
     * 详细信息，请见fota demo使用文档
     * {
     * "md5sum": "88d967d18d962aed43e44d5a627b85a2",
     * "version_name": "com.ada.app.wifistrengthen_1.1.9_19 (1).apk",
     * "cycle": "60",
     * "file_size": "1635304",
     * "download_url": "http://fotadown.mayitek.com/ota/root_data02_2/fotaopen/uploadApk/adups/raise_0627A/MSM8x10/android/2016081711062460413.apk",
     * "notify": "1",
     * "has_update": "1",  //1表示有更新
     * "version_code": "19"
     * }
     */

    private String download_url;
    private int file_size;
    private String version_name;
    private int version_code;
    private String md5sum;
    private String notify;
    private String cycle;

    private String has_update;

    private static UpdateInfo m_instance;

    // Gson解析
    public UpdateInfo() {
        // 空实现
    }

    public static UpdateInfo getInstance() {
        if (m_instance == null) {
            synchronized (UpdateInfo.class) {
                if (m_instance == null) {
                    m_instance = new UpdateInfo();

                }
            }
        }
        //数据持久化读取
            m_instance.init(SPFTool.getString(UpgradeApplication.s_context, KEY_UPDATEINFO_DATA, ""));
        return m_instance;
    }

    /**
     * 初始化自升级数据
     *
     * @param json
     */
    public void init(String json) {
        if (!TextUtils.isEmpty(json)) {
            try {
                m_instance = new Gson().fromJson(json, UpdateInfo.class);
                SPFTool.putString(UpgradeApplication.s_context, KEY_UPDATEINFO_DATA, json);
            } catch (JsonSyntaxException e) {
                m_instance = null;
                e.printStackTrace();
            }
        }
    }

    /**
     * 清除自升级数据
     */
    public void clear() {
        m_instance = null;
        SPFTool.putString(UpgradeApplication.s_context, KEY_UPDATEINFO_DATA, "");
    }

    public String getHas_update() {
        return has_update;
    }

    public void setHas_update(String has_update) {
        this.has_update = has_update;
    }

    public String getCycle() {
        return cycle;
    }

    public void setCycle(String cycle) {
        this.cycle = cycle;
    }

    public String getDownload_url() {
        return download_url;
    }

    public void setDownload_url(String download_url) {
        this.download_url = download_url;
    }

    public int getFile_size() {
        return file_size;
    }

    public void setFile_size(int file_size) {
        this.file_size = file_size;
    }

    public String getVersion_name() {
        return version_name;
    }

    public void setVersion_name(String version_name) {
        this.version_name = version_name;
    }

    public int getVersion_code() {
        return version_code;
    }

    public void setVersion_code(int version_code) {
        this.version_code = version_code;
    }

    public String getMd5sum() {
        return md5sum;
    }

    public void setMd5sum(String md5sum) {
        this.md5sum = md5sum;
    }

    public String getNotify() {
        return notify;
    }

    public void setNotify(String notify) {
        this.notify = notify;
    }

    public boolean has_update() {
        if (TextUtils.isEmpty(has_update)
                || "0".equals(has_update)
                || TextUtils.isEmpty(download_url)
                || file_size <= 0
                || TextUtils.isEmpty(version_name)
                || version_code <= 0
                || TextUtils.isEmpty(md5sum)
                || TextUtils.isEmpty(notify)) {
            return false;
        } else {
            return true;
        }
    }
}
