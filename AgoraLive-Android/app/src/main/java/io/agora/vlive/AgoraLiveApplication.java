package io.agora.vlive;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import io.agora.rtc.RtcEngine;
import io.agora.vlive.agora.AgoraEngine;
import io.agora.vlive.utils.Global;

public class AgoraLiveApplication extends Application {
    private SharedPreferences mPref;
    private Config mConfig;
    private AgoraEngine mAgoraEngine;

    @Override
    public void onCreate() {
        super.onCreate();
        mPref = getSharedPreferences(Global.Constants.SF_NAME, Context.MODE_PRIVATE);
        mConfig = new Config(this);
        mAgoraEngine = new AgoraEngine(this);
    }

    public Config config() {
        return mConfig;
    }

    public SharedPreferences preferences() {
        return mPref;
    }

    public RtcEngine rtcEngine() {
        return mAgoraEngine.rtcEngine();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }
}