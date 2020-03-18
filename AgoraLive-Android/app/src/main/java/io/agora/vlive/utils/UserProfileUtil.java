package io.agora.vlive.utils;

public class UserProfileUtil {
    public static int getUserProfileIcon(String userId) {
        try {
            long intUserId = Long.valueOf(userId);
            int size = Global.Constants.PROFILE_BG_RES.length;
            int index = (int) intUserId % size;
            return Global.Constants.PROFILE_BG_RES[index];
        } catch (NumberFormatException e) {
            return Global.Constants.PROFILE_BG_RES[0];
        }
    }
}
