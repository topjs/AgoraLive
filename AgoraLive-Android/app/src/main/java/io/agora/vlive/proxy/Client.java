package io.agora.vlive.proxy;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import io.agora.vlive.proxy.interfaces.GeneralService;
import io.agora.vlive.proxy.interfaces.LiveRoomService;
import io.agora.vlive.proxy.interfaces.UserService;
import io.agora.vlive.proxy.struts.request.Request;
import io.agora.vlive.proxy.struts.response.AppVersionResponse;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;

public class Client implements Callback<ResponseBody> {
    private static final String MOCK_URL = "http://115.231.168.26:3000/mock/12/";
    private static final int MAX_RESPONSE_THREAD = 10;

    private static Client sInstance;

    private Retrofit mRetrofit;

    private GeneralService mGeneralService;
    private LiveRoomService mLiveRoomService;
    private UserService mUserService;

    private ClientProxyListener mProxyListener;
    private Map<Call, Long> mReqMap = new HashMap<>();

    private Gson mGson;
    private Executor mExecutor;

    private Client() {
        mExecutor = Executors.newFixedThreadPool(MAX_RESPONSE_THREAD);
        mRetrofit = new Retrofit.Builder()
                .baseUrl(MOCK_URL)
                .callbackExecutor(mExecutor)
                .build();
        mGeneralService = mRetrofit.create(GeneralService.class);
        mLiveRoomService = mRetrofit.create(LiveRoomService.class);
        mUserService = mRetrofit.create(UserService.class);
        mGson = new GsonBuilder().create();
    }

    static Client instance() {
        if (sInstance == null) {
            synchronized (Client.class) {
                if (sInstance == null) {
                    sInstance = new Client();
                }
            }
        }
        return sInstance;
    }

    void setProxyListener(ClientProxyListener listener) {
        mProxyListener = listener;
    }

    void getAppVersion(long reqId, String appCode, int osType, int terminalType, String appVersion) {
        mGeneralService.getAppVersion(reqId, Request.APP_VERSION,
                appCode, osType, terminalType, appVersion).enqueue(this);
    }

    void getGiftList(long reqId) {
        mGeneralService.getGiftList(reqId, Request.GIFT_LIST).enqueue(this);
    }

    void getMusicList(long reqId) {
        mGeneralService.getMusicList(reqId, Request.MUSIC_LIST).enqueue(this);
    }

    @Override
    public void onResponse(@NonNull Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
        System.out.println(Thread.currentThread().getName());

        String reqIdString = response.headers().get("reqId");
        long reqId = TextUtils.isEmpty(reqIdString) ? -1 :
            Long.parseLong(reqIdString);

        if (/*reqId == -1 || */response.body() == null) return;

        String json;
        try {
            json = new String(response.body().bytes());
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        String reqTypeString = response.headers().get("reqType");
        int type = TextUtils.isEmpty(reqTypeString) ? -1 :
                Integer.parseInt(reqTypeString);

        switch (type) {
            case Request.APP_VERSION:
                AppVersionResponse verResponse = mGson.fromJson(json, AppVersionResponse.class);
                verResponse.toString();
                break;
            case Request.GIFT_LIST:
            case Request.MUSIC_LIST:
            case Request.OSS:
            case Request.CREATE_USER:
            case Request.EDIT_USER:
            case Request.ROOM_LIST:
            case Request.CREATE_ROOM:
            case Request.ENTER_ROOM:
            case Request.LEAVE_ROOM:
            case Request.AUDIENCE_LIST:
            case Request.SEND_GIFT:
            case Request.GIFT_RANK:
            case Request.SEAT_LIST:
            case Request.MODIFY_SEAT_STATE:
            case Request.REFRESH_TOKEN:
            case Request.PK_START_STOP:
        }
    }

    @Override
    public void onFailure(@NonNull Call<ResponseBody> call, Throwable t) {
        t.printStackTrace();
    }
}
