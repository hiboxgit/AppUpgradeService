package com.dudu.upgrade.fota.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dudu.upgrade.R;
import com.fota.utils.Trace;

/**
 * Created by raise.yang on 2016/04/01.
 */
public class CenterCircleView extends RelativeLayout implements View.OnClickListener {

    private static final int STATE_CHECK_VERSION = 0X1;
    private static final int STATE_START_DOWNLOAD = 0X2;
    private static final int STATE_UPGRADE = 0X3;
    private static final int STATE_IDLE = 0X4; //无法点击的状态
    private static final String TAG = "CenterCircleView";

    private int m_state = STATE_CHECK_VERSION;

    private CommonCircleView m_circle_view;
    private ImageView m_arrow;
    private TextView m_progress_view;
    private TextView m_state_detail_view;

    private OnClickListener m_listener;

    public CenterCircleView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        LayoutInflater.from(getContext()).inflate(R.layout.viewgroup_circle, this);
    }

    public CenterCircleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);

    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        m_circle_view = (CommonCircleView) findViewById(R.id.view_circle);
        m_arrow = (ImageView) findViewById(R.id.image_arrow);
        m_progress_view = (TextView) findViewById(R.id.tv_progress);
        m_state_detail_view = (TextView) findViewById(R.id.tv_update_state_detail);

        m_circle_view.loadXmlAnim(R.anim.iot_rotate);
    }

    public void setListener(OnClickListener listener) {
        this.m_listener = listener;
        setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (m_listener == null) return;
        switch (m_state) {
            case STATE_CHECK_VERSION:
                m_listener.on_check_version();
                break;
            case STATE_START_DOWNLOAD:
                m_listener.on_start_download();
                break;
            case STATE_UPGRADE:
                m_listener.on_reboot_upgrade();
                break;
        }
    }

    public void resetUI_checking_version() {
        Trace.d("CenterCircleView", "resetUI_checking_version() ");
        m_state = STATE_IDLE;
        m_circle_view.setProgress(10);
        m_circle_view.startAnim();
        m_arrow.setVisibility(INVISIBLE);
        m_progress_view.setVisibility(GONE);
        m_state_detail_view.setText(R.string.iot_checking_new_version);

    }

    public void resetUI_can_download() {
        Trace.d(TAG, "resetUI_can_download() ");
        m_state = STATE_START_DOWNLOAD;
        m_circle_view.stopAnim();
        m_circle_view.setProgress(100);
        m_arrow.setRotation(180);
        m_arrow.setVisibility(VISIBLE);
        m_progress_view.setVisibility(GONE);
        m_state_detail_view.setText(R.string.iot_to_download);
    }

    public void resetUI_check_version() {
        m_state = STATE_CHECK_VERSION;
        m_circle_view.stopAnim();
        m_circle_view.setProgress(100);
        m_arrow.setRotation(0);
        m_arrow.setVisibility(VISIBLE);
        m_progress_view.setVisibility(GONE);
        m_state_detail_view.setText(R.string.has_new_version);
        Log.d("CenterCircleView", "resetUI_check_version() ");
    }

    public void resetUI_downloading(int progress) {
        m_state = STATE_IDLE;
        m_circle_view.stopAnim();
        m_circle_view.setProgress(progress);
        m_arrow.setVisibility(GONE);
        m_progress_view.setText(progress + "%");
        m_progress_view.setVisibility(VISIBLE);
        m_state_detail_view.setText("");
    }

    public void resetUI_can_upgrade() {
        m_state = STATE_UPGRADE;
        m_circle_view.stopAnim();
        m_circle_view.setProgress(100);
        m_arrow.setRotation(0);
        m_arrow.setVisibility(VISIBLE);
        m_progress_view.setVisibility(GONE);
        m_state_detail_view.setText(R.string.iot_to_update);
    }

    /**
     * 升级中
     */
    public void resetUI_upgrading() {
        m_state = STATE_IDLE;
        m_circle_view.setProgress(20);
        m_circle_view.startAnim();
        m_arrow.setVisibility(INVISIBLE);
        m_progress_view.setVisibility(GONE);
        m_state_detail_view.setText(R.string.iot_updating_new_version);
    }

    public interface OnClickListener {
        void on_check_version();

        void on_start_download();

        void on_reboot_upgrade();
    }

}
