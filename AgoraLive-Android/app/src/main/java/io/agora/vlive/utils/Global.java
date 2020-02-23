package io.agora.vlive.utils;

import android.content.SharedPreferences;
import android.graphics.Color;

import io.agora.vlive.AgoraLiveApplication;
import io.agora.vlive.R;
import io.agora.vlive.ui.actionsheets.BeautySettingActionSheet;

public class Global {
    private AgoraLiveApplication mApplication;
    private States mStates;

    public Global(AgoraLiveApplication application) {
        mApplication = application;
        mStates = new States();
    }

    public States getGlobalStates() {
        return mStates;
    }

    public static class Constants {
        // Shared reference keys
        public static final String SF_NAME = "sf-agora-live";
        public static final String KEY_BEAUTY_ENABLED = "key-beauty-enabled";
        public static final String KEY_BRIGHTNESS = "key-brightness";
        public static final String KEY_SMOOTH = "key-smooth";
        public static final String KEY_TEMPERATURE = "key-color-temperature";
        public static final String KEY_CONTRAST = "key-contrast";

        public static final String KEY_RESOLUTION = "key-resolution";
        public static final String KEY_FRAME_RATE = "key-framerate";
        public static final String KEY_BITRATE = "key-bitrate";

        public static final String TAB_KEY = "live-tab";
        public static final int TAB_ID_MULTI = 0;
        public static final int TAB_ID_SINGLE = 1;
        public static final int TAB_ID_PK = 2;

        public static final String KEY_ROOM_NAME = "room-name";
        public static final String KEY_IS_HOST = "is-host";

        public static final int DIVIDER_COLOR = Color.rgb(239, 239, 239);

        public static final int VIDEO_MAX_BITRATE = 4000;
        public static final int VIDEO_MIN_BITRATE = 150;
        public static final int VIDEO_DEFAULT_BITRATE = 800;
        public static final int VIDEO_DEFAULT_RESOLUTION_INDEX = 0;
        public static final int VIDEO_DEFAULT_FRAME_RATE_INDEX = 0;

        public static final int[] TAB_IDS_RES = {
                R.string.home_category_title_multi,
                R.string.home_category_title_single,
                R.string.home_category_title_pk
        };

        public static final String[] RESOLUTIONS = {
                "1920x1080",
                "1280x720",
                "960x640",
                "640x480"
        };

        public static final String[] FRAME_RATES = {
                "15", "24", "30"
        };
    }

    public class States {
        private int mLastTabPosition = Constants.TAB_ID_MULTI;

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

        States() {
            SharedPreferences sp = mApplication.preferences();

            mBeautyEnabled = sp.getBoolean(Constants.KEY_BEAUTY_ENABLED, true);

            //TODO the default value should come from FaceUnity module
            // will be completed soon
            mBrightnessValue = sp.getFloat(Constants.KEY_BRIGHTNESS, 0.3f);
            mSmoothValue = sp.getFloat(Constants.KEY_SMOOTH, 0.3f);
            mColorTemperatureValue = sp.getFloat(Constants.KEY_TEMPERATURE, 0.3f);

            mContrast = sp.getInt(Constants.KEY_CONTRAST,
                    BeautySettingActionSheet.CONTRAST_MEDIUM);

            mResolutionIndex = sp.getInt(Constants.KEY_RESOLUTION, Constants.VIDEO_DEFAULT_RESOLUTION_INDEX);
            mFrameRateIndex = sp.getInt(Constants.KEY_FRAME_RATE, Constants.VIDEO_DEFAULT_FRAME_RATE_INDEX);
            mBitrate = sp.getInt(Constants.KEY_BITRATE, Constants.VIDEO_DEFAULT_BITRATE);
        }

        public boolean isBeautyEnabled() {
            return mBeautyEnabled;
        }

        public void setBeautyEnabled(boolean enabled) {
            mBeautyEnabled = enabled;
            mApplication.preferences().edit()
                    .putBoolean(Constants.KEY_BEAUTY_ENABLED, enabled).apply();
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
                   .putFloat(Constants.KEY_BRIGHTNESS, brightness).apply();
        }

        public float beautySmooth() {
            return mSmoothValue;
        }

        public void setBeautySmooth(float smooth) {
            mSmoothValue = smooth;
            mApplication.preferences().edit()
                    .putFloat(Constants.KEY_SMOOTH, smooth).apply();
        }

        public float beautyColorTemp() {
            return mColorTemperatureValue;
        }

        public void setBeautyColorTemp(float temperature) {
            mColorTemperatureValue = temperature;
            mApplication.preferences().edit()
                    .putFloat(Constants.KEY_TEMPERATURE, temperature).apply();
        }

        public int beautyContrast() {
            return mContrast;
        }

        public void setContrast(int contrast) {
            mContrast = contrast;
            mApplication.preferences().edit()
                    .putInt(Constants.KEY_CONTRAST, contrast).apply();
        }

        public int resolutionIndex() {
            return mResolutionIndex;
        }

        public void setResolutionIndex(int index) {
            mResolutionIndex = index;
            mApplication.preferences().edit()
                    .putInt(Constants.KEY_RESOLUTION, index).apply();
        }

        public int frameRateIndex() {
            return mFrameRateIndex;
        }

        public void setFrameRateIndex(int index) {
            mFrameRateIndex = index;
            mApplication.preferences().edit()
                    .putInt(Constants.KEY_FRAME_RATE, index).apply();
        }

        public int videoBitrate() {
            return mBitrate;
        }

        public void setVideoBitrate(int bitrate) {
            mBitrate = bitrate;
            mApplication.preferences().edit()
                    .putInt(Constants.KEY_BITRATE, bitrate).apply();
        }
    }

    public static class FakeData {
        public static final String[][] BG_MUSIC = {
            {"像梦一样自由", "汪峰"},
            {"我就知道那是爱", "梁静茹"},
            {"I Believe I Can Fly", "R.Kelly"},
            {"What a wonderful world", "Louis Armstrong"},
            {"长安长安", "郑钧"},
            {"同桌的你", "老狼"},
            {"天下没有不散的宴席", "郑钧"},
            {"作曲家", "李荣浩"},
        };
    }
}
