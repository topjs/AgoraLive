package io.agora.vlive.agora;

import androidx.annotation.NonNull;

import io.agora.rtc.Constants;
import io.agora.rtc.RtcEngine;
import io.agora.vlive.AgoraLiveApplication;
import io.agora.vlive.R;

public class AgoraEngine {
    private static final String TAG = AgoraEngine.class.getSimpleName();

    private AgoraLiveApplication mApplication;
    private RtcEngine mRtcEngine;
    private AgoraRtcHandler mRtcEventHandler;

    public AgoraEngine(@NonNull AgoraLiveApplication application) {
        mApplication = application;
    }

    public RtcEngine rtcEngine() {
        if (mRtcEngine == null) {
            synchronized (AgoraEngine.class) {
                if (mRtcEngine == null) {
                    mRtcEventHandler = new AgoraRtcHandler();
                    try {
                        mRtcEngine = RtcEngine.create(mApplication, mApplication.
                                getResources().getString(R.string.private_app_id), mRtcEventHandler);
                        mRtcEngine.enableVideo();
                        mRtcEngine.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING);
                        mRtcEngine.enableDualStreamMode(false);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return mRtcEngine;
    }

    public void registerRtcHandler(EventHandler handler) {
        if (mRtcEventHandler != null) mRtcEventHandler.registerEventHandler(handler);
    }

    public void removeRtcHandler(EventHandler handler) {
        if (mRtcEventHandler != null) mRtcEventHandler.removeEventHandler(handler);
    }
}
