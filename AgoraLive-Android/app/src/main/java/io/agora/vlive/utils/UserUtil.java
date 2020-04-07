package io.agora.vlive.utils;

import android.content.Context;
import android.os.Build;
import android.os.Environment;

import java.io.File;

public class UserUtil {
    private static final String LOG_FOLDER_NAME = "logs";
    private static final String LOG_FILE_NAME_RTC = "agora-rtc.log";
    private static final String LOG_FILE_NAME_RTM = "agora-rtm.log";

    public static int getUserProfileIcon(String userId) {
        try {
            long intUserId = Long.valueOf(userId);
            int size = Global.Constants.PROFILE_BG_RES.length;
            int index = (int) (intUserId % size);
            return Global.Constants.PROFILE_BG_RES[index];
        } catch (NumberFormatException e) {
            return Global.Constants.PROFILE_BG_RES[0];
        }
    }

    public static String rtcLogFilePath(Context context) {
        return logFilePath(context, LOG_FILE_NAME_RTC);
    }

    public static String rtmLogFilePath(Context context) {
        return logFilePath(context, LOG_FILE_NAME_RTM);
    }

    private static String logFilePath(Context context, String name) {
        File folder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            folder = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), LOG_FOLDER_NAME);
        } else {
            String path = Environment.getExternalStorageDirectory()
                    .getAbsolutePath() + File.separator +
                    context.getPackageName() + File.separator +
                    LOG_FOLDER_NAME;
            folder = new File(path);
            if (!folder.exists() && !folder.mkdir()) folder = null;
        }

        if (folder != null && !folder.exists() && !folder.mkdir()) return "";
        else return new File(folder, LOG_FILE_NAME_RTC).getAbsolutePath();
    }
}
