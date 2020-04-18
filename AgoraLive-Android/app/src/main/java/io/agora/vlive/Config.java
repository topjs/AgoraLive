package io.agora.vlive;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;

import io.agora.framework.camera.Constant;
import io.agora.rtc.video.VideoEncoderConfiguration;
import io.agora.vlive.proxy.struts.model.AppVersionInfo;
import io.agora.vlive.proxy.struts.model.GiftInfo;
import io.agora.vlive.proxy.struts.model.MusicInfo;
import io.agora.vlive.ui.actionsheets.BeautySettingActionSheet;
import io.agora.vlive.utils.GiftUtil;
import io.agora.vlive.utils.Global;

public class Config {
    public static class UserProfile {
        private String userId;
        private String userName;
        private String imageUrl;
        private String token;
        private String rtcToken;
        private String rtmToken;
        private long agoraUid;
        private SoftReference<Drawable> userIcon;

        public boolean isValid() {
            return userId != null;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getUserName() {
            return userName != null ? userName : userId;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public String getRtcToken() {
            return rtcToken;
        }

        public void setRtcToken(String rtcToken) {
            this.rtcToken = rtcToken;
        }

        public String getRtmToken() {
            return rtmToken;
        }

        public void setRtmToken(String rtmToken) {
            this.rtmToken = rtmToken;
        }

        public long getAgoraUid() {
            return agoraUid;
        }

        public void setAgoraUid(long agoraUid) {
            this.agoraUid = agoraUid;
        }

        public Drawable getProfileIcon() {
            return userIcon == null ? null : userIcon.get();
        }

        public void setProfileIcon(Drawable userProfileDrawable) {
            this.userIcon = new SoftReference<>(userProfileDrawable);
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
    private AppVersionInfo mVersionInfo;
    private List<GiftInfo> mGiftInfoList = new ArrayList<>();
    private List<MusicInfo> mMusicInfoList = new ArrayList<>();
    private int mLastTabPosition = Global.Constants.TAB_ID_MULTI;

    // Camera capture configurations
    private int mCameraFacing = Constant.CAMERA_FACING_FRONT;

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

    // rtc configurations
    private boolean mVideoMuted;
    private boolean mAudioMuted;

    public UserProfile getUserProfile() {
        return mUserProfile;
    }

    public AppVersionInfo getVersionInfo() {
        return mVersionInfo;
    }

    public void setVersionInfo(AppVersionInfo mVersionInfo) {
        this.mVersionInfo = mVersionInfo;
    }

    public boolean hasCheckedVersion() {
        return mVersionInfo != null;
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

    public int getCameraFacing() {
        return mCameraFacing;
    }

    public void setCameraFacing(int facing) {
        this.mCameraFacing = facing;
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

    public VideoEncoderConfiguration createVideoEncoderConfig() {
        return new VideoEncoderConfiguration(
                Global.Constants.RESOLUTIONS[mResolutionIndex],
                Global.Constants.FRAME_RATES[mFrameRateIndex],
                mBitrate,
                VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_ADAPTIVE
        );
    }

    public int currentMusicIndex() {
        return mCurrentPlayedMusicIndex;
    }

    public void setCurrentMusicIndex(int index) {
        mCurrentPlayedMusicIndex = index;
    }

    public void setVideoMuted(boolean muted) {
        mVideoMuted = muted;
    }

    public boolean isVideoMuted() {
        return mVideoMuted;
    }

    public void setAudioMuted(boolean muted) {
        mAudioMuted = muted;
    }

    public boolean isAudioMuted() {
        return mAudioMuted;
    }

    public void initGiftList(Context context) {
        String[] mGiftNames = context.getResources().getStringArray(R.array.gift_names);
        int[] mGiftValues = context.getResources().getIntArray(R.array.gift_values);
        mGiftInfoList = new ArrayList<>();
        for (int i = 0; i < mGiftNames.length; i++) {
            GiftInfo info = new GiftInfo(i, mGiftNames[i],
                    GiftUtil.GIFT_ICON_RES[i], mGiftValues[i]);
            mGiftInfoList.add(i, info);
        }
    }

    public List<GiftInfo> getGiftList() {
        return mGiftInfoList;
    }

    public void setMusicList(List<MusicInfo> list) {
        mMusicInfoList.clear();
        mMusicInfoList.addAll(list);
    }

    public List<MusicInfo> getMusicList() {
        return mMusicInfoList;
    }
}
