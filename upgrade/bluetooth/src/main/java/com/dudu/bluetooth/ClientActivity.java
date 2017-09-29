package com.dudu.bluetooth;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.RecoverySystem;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.dudu.bluetooth.adapter.BTAdapter;
import com.fota.bluetooth.protocol.TransferPackageInfo;
import com.fota.bluetooth.service.BluetoothFotaService;
import com.fota.bluetooth.utils.BluetoothUtils;
import com.fota.bluetooth.utils.Constants;
import com.fota.iport.info.MobileParamInfo;
import com.fota.utils.Trace;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * 智能设备端界面
 */
public class ClientActivity extends Activity implements AdapterView.OnItemClickListener {

    private static final String TAG = "ClientActivity";
    private TextView mTips;

    private ProgressBar mProgressBar;

    private ListView listView;
    private BTAdapter mAdapter;

    private Set<BluetoothDevice> mdatas;

    private BluetoothFotaService mService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);
        mTips = (TextView) findViewById(R.id.textview_display_info);
        mProgressBar = (ProgressBar) findViewById(R.id.progressbar);
        listView = (ListView) findViewById(R.id.listview_bottom);

        mdatas = new HashSet<>();
        mAdapter = new BTAdapter(new ArrayList<>(mdatas), this);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(this);

        // 初始化蓝牙传输服务类
        mService = new BluetoothFotaService(this, mServiceHandler);
        dialog = new AlertDialog.Builder(this)
                .setTitle("蓝牙连接")
                .setMessage("正在连接到蓝牙设备")
                .create();
        // 蓝牙操作工具类
        BluetoothUtils.init(mBluetoothUtilHandler, 20 * 1000);
        // 设置升级包保存路径
        if (BTApplication.is_test) {
            mService.client_set_file_path(Environment.getExternalStorageDirectory().getAbsolutePath() + "/update.zip");
        } else {
            mService.client_set_file_path("/cache/update.zip");
        }
    }

    public void click_start_discovery(View view) {
        // 查找手机端蓝牙设备
        BluetoothUtils.start_discovery(this);
    }

    private int down_progress;

    private Handler mServiceHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {

                case Constants.MESSAGE_DEVICE_NAME:
//                    Toast.makeText(ClientActivity.this, "连接上蓝牙设备：" + ((BluetoothDevice) msg.obj).getName(), Toast.LENGTH_SHORT).show();
                    printf("连接上蓝牙设备：" + ((BluetoothDevice) msg.obj).getName());
                    dialog.dismiss();
                    // 将穿戴设备端，系统版本号发给手机端
                    mService.client_check_version(getDeviceProjectParmas());
                    break;
                case Constants.MESSAGE_READ_STRING:
                    printf("server:" + msg.obj);
                    switch (msg.arg1) {
                        case TransferPackageInfo.PACKAGE_TYPE_SERVER_HAS_NEW_VERSION:
                            Log.d("ClientActivity", "客户端请求服务器发送update文件.");
                            mService.client_transfer_file_start();
                            break;
                        case TransferPackageInfo.PACKAGE_TYPE_SERVER_IS_LATEST_VERSION:
                            Toast.makeText(ClientActivity.this, "已是最新版本.", Toast.LENGTH_SHORT).show();

                            break;
                    }
                    break;
                case Constants.MESSAGE_READ_FILE:
                    // 接收文件进度
                    int progress = msg.arg1;
                    // 接收文件总大小
                    int total_size = msg.arg2;
                    // 保存文件路径
                    String file_path = (String) msg.obj;
                    if (down_progress != progress) {
                        down_progress = progress;
                        printf(String.format("downloading,progress:%s,total_size:%s,file_path:%s", progress, total_size, file_path));
                        //下载进度，发给服务端
                        mService.client_post_progress(String.valueOf(progress));
                        if (down_progress == 100) {
                            receiver_file_finished();
                        }
                    }
                    break;
                case Constants.MESSAGE_STATE_CHANGE:
                    if (msg.arg1 == BluetoothFotaService.STATE_CONNECTING) {
                        dialog.show();
                    } else {
                        if (dialog.isShowing())
                            dialog.dismiss();
                    }
                    break;
            }
            return true;
        }
    });

    private void receiver_file_finished() {
        if (BTApplication.is_test) {
            Toast.makeText(ClientActivity.this, "升级包接收完成，开始升级", Toast.LENGTH_SHORT).show();
        } else {
            try {
                RecoverySystem.installPackage(this, new File("/cache/update.zip"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Handler mBluetoothUtilHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case BluetoothUtils.WHAT_FOUND_BT_DEVICE:
                    // 找到蓝牙设备
                    mdatas.add((BluetoothDevice) msg.obj);
                    mAdapter.setDatas(new ArrayList<>(mdatas));
                    break;
                case BluetoothUtils.WHAT_START_DISCOVERY:
                    // 开始查找蓝牙设备
                    mProgressBar.setVisibility(View.VISIBLE);
                    mdatas.clear();
                    mAdapter.setDatas(new ArrayList<>(mdatas));
                    break;
                case BluetoothUtils.WHAT_CANCEL_DISCOVERY:
                    // 取消查找蓝牙设备
                    mProgressBar.setVisibility(View.GONE);
                    break;
            }
            return true;
        }
    });

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//        BluetoothUtils.cancel_discovery(this);
        BluetoothDevice item = mAdapter.getItem(position);
        Log.d("ClientActivity", "connect:" + item.getName());
        // 连接手机端(蓝牙连接)
        mService.connect(item, true);
    }

    AlertDialog dialog;

    /**
     * 显示提示消息
     *
     * @param tips
     */
    private void printf(String tips) {
        mTips.setText(mTips.getText() + "\n" + tips);
    }


    public void click_cancel_discovery(View view) {
        BluetoothUtils.cancel_discovery(this);
    }

    public void click_test(View view) {

    }

    public String getDeviceProjectParmas() {
        MobileParamInfo mobileParamInfo = MobileParamInfo.getInstance();
        JSONObject jo = new JSONObject();
        try {
            jo.put("mac", mobileParamInfo.mac);
            jo.put("mid", mobileParamInfo.mid);
            jo.put("version", mobileParamInfo.version);
            jo.put("oem", mobileParamInfo.oem);
            jo.put("models", mobileParamInfo.models);
            jo.put("token", mobileParamInfo.token);
            jo.put("platform", mobileParamInfo.platform);
            jo.put("deviceType", mobileParamInfo.deviceType);
        } catch (JSONException e) {
            e.printStackTrace();
            Trace.d(TAG, "getDeviceProjectParmas() e = " + e.getMessage());
            return "null";
        }
        Trace.d(TAG, "getDeviceProjectParmas() result = " + jo.toString());
        return jo.toString();
    }
}
