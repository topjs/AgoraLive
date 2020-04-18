package io.agora.vlive.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;

import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import java.io.File;

import io.agora.vlive.proxy.struts.model.UserProfile;

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

    public static Drawable getUserRoundIcon(Resources resources, String userId) {
        int res = UserUtil.getUserProfileIcon(userId);
        RoundedBitmapDrawable drawable = RoundedBitmapDrawableFactory.create(
                resources, BitmapFactory.decodeResource(resources, res));
        drawable.setCircular(true);
        return drawable;
    }

    public static String getUserText(String userId, String userName) {
        return !TextUtils.isEmpty(userName) ? userName : userId;
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
