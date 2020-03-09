package io.agora.vlive;

import android.content.SharedPreferences;

import io.agora.vlive.ui.actionsheets.BeautySettingActionSheet;
import io.agora.vlive.utils.Global;

public class Config {
    public static class UserProfile {
        public String userId;
        public String userName;
        public String imageUrl;
        public String token;

        public boolean isValid() {
            return userId != null && userName != null && token != null;
        }
    }

    private AgoraLiveApplication mApplication;

    Config(AgoraLiveApplication application) {
        mApplication = application;
        mUserProfile = new UserProfile();
        SharedPreferences sp = mApplication.preferences();

        mBeautyEnabled = sp.getBoolean(Global.Constants.KEY_BEAUTY_ENABLED, true);

        // The default value should come from FaceUnity
        // module, which will be completed soon
        mBrightnessValue = sp.getFloat(Global.Constants.KEY_BRIGHTNESS, 0.3f);
        mSmoothValue = sp.getFloat(Global.Constants.KEY_SMOOTH, 0.3f);
        mColorTemperatureValue = sp.getFloat(Global.Constants.KEY_TEMPERATURE, 0.3f);

        mContrast = sp.getInt(Global.Constants.KEY_CONTRAST,
                BeautySettingActionSheet.CONTRAST_MEDIUM);

        mResolutionIndex = sp.getInt(Global.Constants.KEY_RESOLUTION, Global.Constants.VIDEO_DEFAULT_RESOLUTION_INDEX);
        mFrameRateIndex = sp.getInt(Global.Constants.KEY_FRAME_RATE, Global.Constants.VIDEO_DEFAULT_FRAME_RATE_INDEX);
        mBitrate = sp.getInt(Global.Constants.KEY_BITRATE, Global.Constants.VIDEO_DEFAULT_BITRATE);
    }

    private UserProfile mUserProfile;
    private int mLastTabPosition = Global.Constants.TAB_ID_MULTI;

    // Beautification configs
    private boolean mBeautyEnabled;
    private float mBrightnessValue;
    private float mSmoothValue;
    private float mColorTemperatureValue;
    private int mContrast;

    // Video configs
    private int mResolutionIndex;
    private int mFrameRateIndex;
    private int mBitrate;

    private int mCurrentPlayedMusicIndex = -1;

    public UserProfile getUserProfile() {
        return mUserProfile;
    }

    public boolean isBeautyEnabled() {
        return mBeautyEnabled;
    }

    public void setBeautyEnabled(boolean enabled) {
        mBeautyEnabled = enabled;
        mApplication.preferences().edit()
                .putBoolean(Global.Constants.KEY_BEAUTY_ENABLED, enabled).apply();
    }

    public int lastTabPosition() {
        return mLastTabPosition;
    }

    public void setLastTabPosition(int position) {
        mLastTabPosition = position;
    }

    public float beautyBrightness() {
        return mBrightnessValue;
    }

    public void setBeautyBrightness(float brightness) {
        mBrightnessValue = brightness;
        mApplication.preferences().edit()
                .putFloat(Global.Constants.KEY_BRIGHTNESS, brightness).apply();
    }

    public float beautySmooth() {
        return mSmoothValue;
    }

    public void setBeautySmooth(float smooth) {
        mSmoothValue = smooth;
        mApplication.preferences().edit()
                .putFloat(Global.Constants.KEY_SMOOTH, smooth).apply();
    }

    public float beautyColorTemp() {
        return mColorTemperatureValue;
    }

    public void setBeautyColorTemp(float temperature) {
        mColorTemperatureValue = temperature;
        mApplication.preferences().edit()
                .putFloat(Global.Constants.KEY_TEMPERATURE, temperature).apply();
    }

    public int beautyContrast() {
        return mContrast;
    }

    public void setContrast(int contrast) {
        mContrast = contrast;
        mApplication.preferences().edit()
                .putInt(Global.Constants.KEY_CONTRAST, contrast).apply();
    }

    public int resolutionIndex() {
        return mResolutionIndex;
    }

    public void setResolutionIndex(int index) {
        mResolutionIndex = index;
        mApplication.preferences().edit()
                .putInt(Global.Constants.KEY_RESOLUTION, index).apply();
    }

    public int frameRateIndex() {
        return mFrameRateIndex;
    }

    public void setFrameRateIndex(int index) {
        mFrameRateIndex = index;
        mApplication.preferences().edit()
                .putInt(Global.Constants.KEY_FRAME_RATE, index).apply();
    }

    public int videoBitrate() {
        return mBitrate;
    }

    public void setVideoBitrate(int bitrate) {
        mBitrate = bitrate;
        mApplication.preferences().edit()
                .putInt(Global.Constants.KEY_BITRATE, bitrate).apply();
    }

    public int currentMusicIndex() {
        return mCurrentPlayedMusicIndex;
    }

    public void setCurrentMusicIndex(int index) {
        mCurrentPlayedMusicIndex = index;
    }
}
