package com.dudu.upgrade.fota;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;

import com.fota.iport.MobAgentPolicy;
import com.fota.iport.service.DLService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by raise.yang on 2016/05/04.
 */
public class FotaPresenter implements FotaContract.Presenter {

    private static final java.lang.String TAG = "FotaPresenter";
    private Context m_context;
    /**
     * holder the view obj
     */
    private FotaContract.View m_view;

    /**
     * activity 的 onCreate()调用
     *
     * @param view
     * @param context
     */
    public FotaPresenter(FotaContract.View view, Context context) {
        m_view = view;
        m_context = context;
        m_view.set_presenter(this);
//        UpdateService.startService(context);
    }


    @Override
    public void start() {
        m_view.show_default_ui();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
//                check_version();
            }
        }, 500);
    }

    @Override
    public void check_version() {
        m_view.show_checking();
        FotaIntentService.startService(m_context, FotaIntentService.TYPE_CHECK);
    }

    @Override
    public void download() {
        m_view.show_downloading(0);
        FotaIntentService.startService(m_context, FotaIntentService.TYPE_DOWNLOAD);
    }

    /**
     * step three, reboot upgrade
     */
    @Override
    public void reboot_upgrade() {
        m_view.show_upgrading();
        FotaIntentService.startService(m_context, FotaIntentService.TYPE_UPGRADE);
    }

    @Override
    public void download_cancel() {
        DLService.download_cancel();
    }

    @Override
    public String parse_release_note() {
        String l_country = m_context.getResources().getConfiguration().locale.getCountry();
        String l_language = m_context.getResources().getConfiguration().locale.getLanguage();
        String l_language_country = l_language + "_" + l_country;
        String content = "";
        String content_backup = "";
        try {
            JSONArray ja = new JSONArray(MobAgentPolicy.getRelNotesInfo().content);
            for (int i = 0; i < ja.length(); i++) {
                JSONObject jo = (JSONObject) ja.get(i);
                if (jo.has("country")) {
                    String country = jo.getString("country");
                    if (l_language_country.equalsIgnoreCase(country)) {
                        content = jo.getString("content");
                        break;
                    } else if (country.contains(l_language)) {
                        content_backup = jo.getString("content");
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            content = "";
        }

        if (TextUtils.isEmpty(content)) {
            if (!TextUtils.isEmpty(content_backup)) {
                content = content_backup;
            } else {
                content = MobAgentPolicy.getRelNotesInfo().content;
            }
        }
        return content;
    }
}
