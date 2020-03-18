package io.agora.vlive.agora;

import androidx.annotation.NonNull;

import io.agora.rtc.Constants;
import io.agora.rtc.RtcEngine;
import io.agora.rtm.RtmClient;
import io.agora.vlive.AgoraLiveApplication;
import io.agora.vlive.R;
import io.agora.vlive.agora.rtm.RtmMessageManager;

public class AgoraEngine {
    private static final String TAG = AgoraEngine.class.getSimpleName();

    private AgoraLiveApplication mApplication;
    private RtcEngine mRtcEngine;
    private AgoraRtcHandler mRtcEventHandler = new AgoraRtcHandler();

    private RtmClient mRtmClient;

    public AgoraEngine(@NonNull AgoraLiveApplication application) {
        mApplication = application;
    }

    public RtcEngine rtcEngine() {
        if (mRtcEngine == null) {
            synchronized (AgoraEngine.class) {
                if (mRtcEngine == null) {
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

    public void registerRtcHandler(RtcEventHandler handler) {
        if (mRtcEventHandler != null) mRtcEventHandler.registerEventHandler(handler);
    }

    public void removeRtcHandler(RtcEventHandler handler) {
        if (mRtcEventHandler != null) mRtcEventHandler.removeEventHandler(handler);
    }

    public RtmClient rtmClient() {
        if (mRtmClient == null) {
            synchronized (AgoraEngine.class) {
                if (mRtmClient == null) {
                    try {
                        mRtmClient = RtmClient.createInstance(mApplication, mApplication.
                                getResources().getString(R.string.private_app_id), RtmMessageManager.instance());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return mRtmClient;
    }

    public void release() {
        if (mRtcEngine != null) RtcEngine.destroy();
        if (mRtmClient != null) {
            mRtmClient.logout(null);
            mRtmClient.release();
        }
    }
}
