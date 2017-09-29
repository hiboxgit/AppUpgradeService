package com.dudu.upgrade.utils;

import android.content.Context;
import android.os.Build;
import android.os.Environment;

import com.fota.utils.Trace;

import java.io.File;

/**
 * Created by raise.yang on 2016/05/06.
 */
public class StorageUtils {
    /**
     * get the update.zip file absolute path.
     * such: ../adupsfota/update.zip
     * <p/>
     * it can create the folder of file parent
     *
     * @param context
     * @return note: it will return "" while the path is unavailable
     */
    public static String get_update_file_path(Context context) {
        String path = "";
        int sdk_int = Build.VERSION.SDK_INT;
        if (sdk_int < 21) {
            // the android version before 5.0
            path = build_external_path() + "/adupsfota/update.zip";
        } else if (sdk_int < 23) {
            // the android version between 5.0 to 6.0
            path = context.getFilesDir() + "/adupsfota/update.zip";
        } else {
            // the android version after 6.0
            path = context.getFilesDir() + "/adupsfota/update.zip";
        }
        File file = new File(path);
        if (!file.getParentFile().exists()) {
            if (!file.getParentFile().mkdirs())
                path = "";
        }
        Trace.d("StorageUtils" ,"get_update_file_path() path = "+path);
        return path;
    }


    public static String build_external_path() {
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }

}
