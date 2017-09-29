package com.dudu.upgrade.bt;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.dudu.upgrade.UpgradeApplication;
import com.fota.bluetooth.protocol.TransferPackageInfo;
import com.fota.bluetooth.service.BluetoothFotaService;
import com.fota.bluetooth.utils.Constants;
import com.fota.iport.MobAgentPolicy;
import com.fota.utils.Trace;
import com.fota.iport.info.MobileParamInfo;
import com.fota.iport.inter.ICheckVersionCallback;
import com.fota.iport.inter.IOnDownloadListener;
import com.fota.iport.service.DLService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

/**
 * Created by raise.yang on 2016/05/09.
 */
public class BtPresenter implements BtContract.Presenter {

    public static final int STATUS_OPENING = 0X0;
    public static final int STATUS_OBTAIN_PARAMS = 0X1;
    public static final int STATUS_CHECK_VERSION = 0X2;
    public static final int STATUS_DOWNLOADING = 0X3;
    public static final int STATUS_TRANSFERING_FILE = 0X4;
    public static final int STATUS_TRANSFER_FILE_SUCCESS = 0X5;


    public static final int DISCOVERABLE_DURATION = 300;
    public static final int REQUEST_ENABLE_DISCOVERY = 0x1;
    private static final String TAG = "BtPresenter";
    private boolean hasDevice;

    private Context mCtx;
    private BtContract.View m_view;
    private BluetoothFotaService m_bt_service;
    private Handler m_handler;
    private boolean isDiscoverable;

    public BtPresenter(Context context, BtContract.View view) {
        m_view = view;
        m_view.set_presenter(this);
        mCtx = context;
        m_handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    case Constants.MESSAGE_DEVICE_NAME:
                        m_view.connect_device(((BluetoothDevice) msg.obj).getName());
                        m_view.notify_view_change(STATUS_OBTAIN_PARAMS);
                        break;
                    case Constants.MESSAGE_READ_STRING:
                        handle_string_message(msg);
                        break;
                    case Constants.MESSAGE_STATE_CHANGE:
                        m_view.connect_state_change(msg.arg1);
                        hasDevice = msg.arg1 == BluetoothFotaService.STATE_CONNECTED ? true : false;
                        break;
                    case Constants.MESSAGE_ERROR:
                        Trace.d(TAG, "handleMessage() MESSAGE_ERROR error = " + msg.obj);
                        m_view.show_error(msg.obj + "");
                        break;
                    case Constants.MESSAGE_READ_FILE:
                        Trace.d(TAG, "handleMessage() MESSAGE_READ_FILE error = " + msg.obj);
                        m_view.show_error(msg.obj + "");
                        break;
                }
                return true;
            }
        });
        m_bt_service = new BluetoothFotaService(context, m_handler);
    }

    private void handle_string_message(Message msg) {
        switch (msg.arg1) {
            case TransferPackageInfo.PACKAGE_TYPE_CLIENT_CHECK_VERSION:
                // 接收到版本信息
                String json = (String) msg.obj;
                boolean success = initDeviceInfo(json);
                if (success) {
                    m_view.notify_view_change(STATUS_CHECK_VERSION);
                    MobAgentPolicy.checkVersion(mCtx, m_checkVersionCallback);
                } else {
                    m_view.show_error("remote device parameters is invalid.");
                }
                break;
            case TransferPackageInfo.PACKAGE_TYPE_CLIENT_TRANSFERING_FILE:
                int progress = 0;
                try {
                    progress = Integer.parseInt((String) msg.obj);
//                    m_view.printf_log("wearable received：" + progress + " %");
                    if (progress == 100) {
                        m_view.notify_view_change(STATUS_TRANSFER_FILE_SUCCESS);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    progress = -1;
                }
                m_view.send_progress(progress);
                break;
        }
    }

    private void start_download() {
        Trace.d(TAG, "start_download() ");
        m_view.notify_view_change(STATUS_DOWNLOADING);
        File downFile = new File(UpgradeApplication.s_update_package_absolute_path);
        DLService.start(mCtx, MobAgentPolicy.getVersionInfo().deltaUrl,
                downFile.getParentFile(), downFile.getName(), m_onDownloadListener
        );
    }

    ICheckVersionCallback m_checkVersionCallback = new ICheckVersionCallback() {
        @Override
        public void onCheckSuccess(int status) {
            start_download();
        }

        @Override
        public void onCheckFail(int status, String errorMsg) {
            m_view.show_error(errorMsg);
        }

        @Override
        public void onInvalidDate() {
            m_view.show_error("detect version InvalidDate");
        }
    };
    IOnDownloadListener m_onDownloadListener = new IOnDownloadListener() {
        @Override
        public void onDownloadProgress(String tmpPath, int totalSize, int downloadedSize) {
            int progress = (int) (downloadedSize * 100.0f / totalSize);
            m_view.send_progress(progress);
        }

        @Override
        public void onDownloadFinished(int state, File file) {
            Trace.d("BtPresenter", "send file start.");
            m_view.notify_view_change(STATUS_TRANSFERING_FILE);
            m_bt_service.server_transfer_file(UpgradeApplication.s_update_package_absolute_path);
        }

        @Override
        public void onDownloadError(int error) {
            m_view.show_error("download file from net error. code = " + error);
        }
    };

    private boolean initDeviceInfo(String json) {
        Trace.d(TAG, "initDeviceInfo() start. json = "+json);
        if (!TextUtils.isEmpty(json)) {
            try {
                JSONObject jsonObject = new JSONObject(json);
                String mac = jsonObject.getString("mac");
                String mid = jsonObject.getString("mid");
                String version = jsonObject.getString("version");
                String oem = jsonObject.getString("oem");
                String models = jsonObject.getString("models");
                String token = jsonObject.getString("token");
                String platform = jsonObject.getString("platform");
                String deviceType = jsonObject.getString("deviceType");
                MobileParamInfo.initInfo(mCtx, mid, version, oem, models, token, platform, deviceType);
                MobileParamInfo.getInstance().mac = mac;
            } catch (JSONException e) {
                e.printStackTrace();
                return false;
            }
        }
        Trace.d(TAG, "initDeviceInfo() end");
        return MobileParamInfo.isValid();
    }

    @Override
    public void start() {
//        discoverable_bt();
    }

    @Override
    public void discoverable_bt() {
        if (!isDiscoverable)
            m_view.can_find_bt();
    }

    @Override
    public void on_result(int requestCode, int resultCode) {
        if (resultCode == Activity.RESULT_CANCELED) {
            if (requestCode == REQUEST_ENABLE_DISCOVERY) {
                m_view.finish();
            }
        } else {
            if ((requestCode == REQUEST_ENABLE_DISCOVERY)) {
                isDiscoverable = true;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        isDiscoverable = false;
                        if (!hasDevice)
                            discoverable_bt();
                    }
                }, DISCOVERABLE_DURATION * 1000);
                // start transfer service
                try {
                    m_bt_service.start();
                } catch (Exception e) {
                    Trace.d("BtPresenter", "on_result() " + e.toString());
                    e.printStackTrace();
                }
                m_view.notify_view_change(STATUS_OPENING);
            }
        }
    }

}
