package com.dudu.upgrade.bt;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.dudu.upgrade.R;

/**
 * 此界面为蓝牙传输更新包使用，可忽略
 */
public class BtServerActivity extends ActionBarActivity implements BtContract.View<BtPresenter> {

    private TextView m_primary_tv;
    private TextView m_tips_tv;
    private ProgressDialog m_dialog;
    private BtPresenter m_presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bt_server_1);

        m_primary_tv = (TextView) findViewById(R.id.textview_center_primary);
        m_tips_tv = (TextView) findViewById(R.id.textview_error_tips);
        m_dialog = new ProgressDialog(this);

        initUIandStatus();
        new BtPresenter(this, this);
    }

    private View.OnClickListener open_listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            m_presenter.discoverable_bt();
            set_primary_text("正在打开FOTA服务");
            m_primary_tv.setOnClickListener(null);
            m_tips_tv.setText("");
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        m_presenter.start();
    }

    @Override
    public void set_presenter(BtPresenter presenter) {
        m_presenter = presenter;
    }

    @Override
    public void can_find_bt() {
        Log.d("BtServerActivity", "can_find_bt() ");
        Intent discoverableIntent = new
                Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, BtPresenter.DISCOVERABLE_DURATION);
        startActivityForResult(discoverableIntent, BtPresenter.REQUEST_ENABLE_DISCOVERY);
    }

    @Override
    public void send_progress(int progress) {
        m_dialog.setProgress(progress);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        m_presenter.on_result(requestCode, resultCode);
    }

    public void set_primary_text(String tip) {
        m_primary_tv.setText(tip);
    }

    @Override
    public void connect_device(String deviceName) {
        set_primary_text("设备已连接");
    }

    @Override
    public void show_error(String e) {
        m_tips_tv.setText("error：" + e);
        m_dialog.dismiss();
        initUIandStatus();
    }

    private void initUIandStatus() {
        set_primary_text("点击打开服务");
        m_primary_tv.setOnClickListener(open_listener);
    }

    @Override
    public void connect_state_change(int state) {


    }

    @Override
    public void notify_view_change(int status) {
        switch (status) {
            case BtPresenter.STATUS_OPENING:
                set_primary_text("服务开启");
                break;
            case BtPresenter.STATUS_OBTAIN_PARAMS:
                m_dialog = new ProgressDialog(this);
                m_dialog.setCancelable(false);
                m_dialog.setMessage("获取远程设备参数中...");
                m_dialog.setIndeterminate(true);
                m_dialog.show();
                break;
            case BtPresenter.STATUS_CHECK_VERSION:
                m_dialog.setMessage("检测版本中...");
                break;
            case BtPresenter.STATUS_DOWNLOADING:
                m_dialog.setMessage("下载差分包中...");
                break;
            case BtPresenter.STATUS_TRANSFERING_FILE:
                m_dialog.setMessage("升级包传输中...");
                break;
            case BtPresenter.STATUS_TRANSFER_FILE_SUCCESS:
                m_dialog.setMessage("传输成功");
                m_dialog.show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                    }
                }, 5 * 1000);
                break;
        }
    }

}
