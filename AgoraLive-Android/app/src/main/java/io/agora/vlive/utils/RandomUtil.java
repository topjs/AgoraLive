package io.agora.vlive.utils;

public class RandomUtil {
    public static int randomProfileBgRes() {
        int size = Global.Constants.PROFILE_BG_RES.length;
        int randomIdx = (int) (Math.random() * size);
        return Global.Constants.PROFILE_BG_RES[randomIdx];
    }
}
