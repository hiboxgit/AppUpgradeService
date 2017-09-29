package com.dudu.upgrade.fota.update;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.dudu.upgrade.fota.policy.PolicyConfig;
import com.fota.iport.MobAgentPolicy;
import com.fota.iport.SPFTool;
import com.fota.iport.service.DLService;
import com.fota.utils.Trace;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class UpdateService extends IntentService {

    private static final String KEY_SELF_UPDATE_CHECK_TIME = "key_self_update_check_time";
    private static final java.lang.String TAG = "UpdateService";
    public static final String KEY_HAS_SELF_UPDATE = "key_has_self_update";
    public static final String KEY_DETECT_CYCLE = "key_detect_cycle";//自更新检测周期
    private static String s_apk_file_path;

    public UpdateService() {
        super("UpdateService");
    }

    public static void startService(Context context) {
        Intent intent = new Intent(context, UpdateService.class);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Trace.d(TAG, "onHandleIntent() ");
        if (intent != null) {
            if (!PolicyConfig.getInstance().self_update) {
                //没有配置自更新
                return;
            }

            //按周期检测
            int cycle = SPFTool.getInt(this, KEY_DETECT_CYCLE, 60);//默认60分钟
            if (cycle >= 60) {//后端配置小于60为不启用
                final long before_time = SPFTool.getLong(this, KEY_SELF_UPDATE_CHECK_TIME, 0);
                long cur_time = System.currentTimeMillis();
                Trace.d(TAG, "onHandleIntent() 自更新间隔时间：" + (cur_time - before_time)/60/1000 + " min.  cycle = " + cycle);
                if (cur_time - before_time > cycle * 60 * 1000) {
                    Trace.d(TAG, "onHandleIntent() 自升级检测周期已到");
                    //检测周期已到
                    UpdateInfo.getInstance().clear();
                    detect_self_update();
                    SPFTool.putLong(this, KEY_SELF_UPDATE_CHECK_TIME, cur_time);
                } else {
                    if (UpdateInfo.getInstance().has_update()) {
                        Trace.d(TAG, "onHandleIntent() 有自升级版本，请求下载自升级包");
                        download_new_apk(UpdateInfo.getInstance());
                    }
                }
            }

//测试代码
//            UpdateInfo info = UpdateInfo.getInstance();
//            info.setDownload_url("http://180.153.93.43/imtt.dd.qq.com/16891/5EA2E2DF1CCFA6DB8BFD3FCE720A1FF4.apk?mkey=57a3366b0acb6f2b&f=1b0c&c=0&fsname=com.u17.comic.phone_3.1.0_3100099.apk&csr=4d5s&p=.apk");
//            download_new_apk(info);
        }
    }

    private void detect_self_update() {

        //自升级请求,若客户有自己的服务器，也可自己配url//请联系杨东升支持
        String result_json = MobAgentPolicy.doPostSelfUpdate(MobAgentPolicy.getSelfUrl(), getAPPVersionCodeFromAPP());
        try {
            //解析后台返回信息
            parseJsonToUpdateInfo(result_json);
        } catch (JSONException e) {
            e.printStackTrace();
            //后端返回数据异常
        }
        UpdateInfo updateInfo = UpdateInfo.getInstance();
        if (updateInfo.has_update()) {
            download_new_apk(updateInfo);
        } else {
            //重置周期
            SPFTool.putLong(this, KEY_SELF_UPDATE_CHECK_TIME, 0);
        }
    }

    private void download_new_apk(UpdateInfo updateInfo) {
        //设置自升级下载路径
        s_apk_file_path = getExternalCacheDir().getAbsolutePath() + "/self.apk";

        //自升级时，若有系统更新包在下载，暂停它
        DLService.download_cancel();

        Trace.d(TAG, "download_new_apk() start.");
        //创建按一个URL实例
        URL url = null;
        BufferedOutputStream bos = null;
        try {
            url = new URL(updateInfo.getDownload_url());
            //创建一个HttpURLConnection的链接对象
            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
            int contentLength = httpConn.getContentLength();
            Trace.d(TAG, "download_new_apk() length = " + contentLength);
            if (new File(s_apk_file_path).length() == 0 || new File(s_apk_file_path).length() != contentLength) {
                //获取所下载文件的InputStream对象
                InputStream inputStream = httpConn.getInputStream();

                byte[] bytes = new byte[1024 >> 3];
                int line;
                bos = new BufferedOutputStream(new FileOutputStream(s_apk_file_path));
                while ((line = inputStream.read(bytes)) != -1) {
                    bos.write(bytes, 0, line);
                }
                inputStream.close();
            }
            //下载完成，通知用户更新
            NoticeSelfActivity.startActivity(UpdateService.this, s_apk_file_path);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bos != null)
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        Trace.d(TAG, "download_new_apk() end.");
    }

    private void parseJsonToUpdateInfo(String result_json) throws JSONException {
        JSONObject jObj = new JSONObject(result_json);
        if (jObj.has("selfUpdate")) {
            String selfStr = jObj.getString("selfUpdate");
            UpdateInfo.getInstance().init(selfStr);
        } else {
            throw new JSONException("后端返回数据异常：json 中不包含selfUpdate字段");
        }
    }

    /**
     * 获取apk的版本号 currentVersionCode
     *
     * @return
     */
    public int getAPPVersionCodeFromAPP() {
        int currentVersionCode = 0;
        PackageManager manager = this.getPackageManager();
        try {
            PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
            currentVersionCode = info.versionCode; // 版本号
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return currentVersionCode;
    }
}
