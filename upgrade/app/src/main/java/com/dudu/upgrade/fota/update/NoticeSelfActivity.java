package com.dudu.upgrade.fota.update;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;

import com.dudu.upgrade.R;

public class NoticeSelfActivity extends Activity {

    public static final String KEY_APK_PATH = "key_apk_path";

    /**
     * 自升级apk的下载路径
     */
    private String apk_path;

    public static void startActivity(Context context, String apk_path) {
        Intent intent = new Intent(context, NoticeSelfActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(KEY_APK_PATH, apk_path);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(android.R.color.transparent)));

        setContentView(R.layout.activity_notice);

        apk_path = getIntent().getStringExtra(KEY_APK_PATH);
        // 弹出dialog框，提示用户更新
        new AlertDialog.Builder(this)
                .setTitle(R.string.remind)
                .setMessage(R.string.self_message)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        intent.setDataAndType(Uri.parse("file://" + apk_path),
                                "application/vnd.android.package-archive");
                        startActivity(intent);
                        finish();
                    }
                })
                .setCancelable(false)//设置不可取消
                .create()
                .show();

    }
}
