package io.agora.vlive;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import io.agora.vlive.utils.Global;

public class AgoraLiveApplication extends Application {
    private Global mGlobal;
    private SharedPreferences mPref;

    @Override
    public void onCreate() {
        super.onCreate();
        mPref = getSharedPreferences(Global.Constants.SF_NAME, Context.MODE_PRIVATE);
        mGlobal = new Global(this);
    }

    public Global global() {
        return mGlobal;
    }

    public Global.States states() {
        return mGlobal.getGlobalStates();
    }

    public SharedPreferences preferences() {
        return mPref;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }
}