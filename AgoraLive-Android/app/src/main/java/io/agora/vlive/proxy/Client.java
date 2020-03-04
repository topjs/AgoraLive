package io.agora.vlive.proxy;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

import io.agora.vlive.proxy.interfaces.GeneralService;
import io.agora.vlive.proxy.interfaces.LiveRoomService;
import io.agora.vlive.proxy.interfaces.RoomListService;
import io.agora.vlive.proxy.interfaces.UserService;
import io.agora.vlive.proxy.struts.request.Request;
import io.agora.vlive.proxy.struts.response.AppVersionResponse;
import io.agora.vlive.proxy.struts.response.AudienceListResponse;
import io.agora.vlive.proxy.struts.response.CreateRoomResponse;
import io.agora.vlive.proxy.struts.response.CreateUserResponse;
import io.agora.vlive.proxy.struts.response.EditUserResponse;
import io.agora.vlive.proxy.struts.response.EnterRoomResponse;
import io.agora.vlive.proxy.struts.response.GiftListResponse;
import io.agora.vlive.proxy.struts.response.GiftRankResponse;
import io.agora.vlive.proxy.struts.response.LeaveRoomResponse;
import io.agora.vlive.proxy.struts.response.ModifySeatStateResponse;
import io.agora.vlive.proxy.struts.response.MusicListResponse;
import io.agora.vlive.proxy.struts.response.OssPolicyResponse;
import io.agora.vlive.proxy.struts.response.RefreshTokenResponse;
import io.agora.vlive.proxy.struts.response.RoomListResponse;
import io.agora.vlive.proxy.struts.response.SeatStateResponse;
import io.agora.vlive.proxy.struts.response.SendGiftResponse;
import io.agora.vlive.proxy.struts.response.StartStopPkResponse;
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
    private RoomListService mRoomListService;
    private LiveRoomService mLiveRoomService;
    private UserService mUserService;

    private ClientProxyListener mProxyListener;
    private Map<Call, Long> mReqMap = new HashMap<>();

    private Gson mGson;

    private Client() {
        mRetrofit = new Retrofit.Builder()
                .baseUrl(MOCK_URL)
                .callbackExecutor(Executors.newFixedThreadPool(MAX_RESPONSE_THREAD))
                .build();
        mGeneralService = mRetrofit.create(GeneralService.class);
        mRoomListService = mRetrofit.create(RoomListService.class);
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

    void requestVersion(long reqId, String appCode, int osType, int terminalType, String appVersion) {
        mGeneralService.requestAppVersion(reqId, Request.APP_VERSION,
                appCode, osType, terminalType, appVersion).enqueue(this);
    }

    void requestGiftList(long reqId) {
        mGeneralService.requestGiftList(reqId, Request.GIFT_LIST).enqueue(this);
    }

    void requestMusicList(long reqId) {
        mGeneralService.requestMusicList(reqId, Request.MUSIC_LIST).enqueue(this);
    }

    void requestOssPolicy(long reqId, String token, int type) {
        mGeneralService.requestOssPolicy(reqId, Request.OSS, token, type);
    }

    void createUser(long reqId, String userName, String avatar) {
        mUserService.createUser(reqId, Request.CREATE_USER, userName, avatar);
    }

    void editUser(long reqId, String token, String userName, String avatar) {
        mUserService.editUser(token, reqId, Request.EDIT_USER, userName, avatar);
    }

    void requestRoomList(long reqId, String auth, int nextId, int count, int type, int pkState) {
        mRoomListService.requestRoomList(reqId, Request.ROOM_LIST, auth, nextId, count, type, pkState);
    }

    void createRoom(long reqId, String token, String userName, int type) {
        mLiveRoomService.createLiveRoom(token, reqId, Request.CREATE_ROOM, userName, type);
    }

    void enterRoom(long reqId, String token, String roomId) {
        mLiveRoomService.enterLiveRoom(token, reqId, Request.ENTER_ROOM, roomId);
    }

    void leaveRoom(long reqId, String token, String roomId) {
        mLiveRoomService.leaveLiveRoom(token, reqId, Request.LEAVE_ROOM, roomId);
    }

    void requestAudienceList(long reqId, String token, String roomId) {
        mLiveRoomService.requestAudienceList(token, reqId, Request.AUDIENCE_LIST, roomId);
    }

    void requestSeatState(long reqId, String token, String roomId) {
        mLiveRoomService.requestSeatState(token, reqId, Request.SEAT_STATE, roomId);
    }

    void modifySeatState(long reqId, String token, String roomId, int no, String userId, int state) {
        mLiveRoomService.modifySeatState(token, reqId, Request.MODIFY_SEAT_STATE, roomId, no, userId, state);
    }

    void sendGift(long reqId, String roomId, String giftId, int count) {
        mLiveRoomService.sendGift(reqId, Request.SEND_GIFT, roomId, giftId, count);
    }

    void giftRank(long reqId, String roomId) {
        mLiveRoomService.giftRank(reqId, Request.GIFT_RANK, roomId);
    }

    void refreshToken(long reqId, String roomId) {
        mGeneralService.refreshToken(reqId, Request.REFRESH_TOKEN, roomId);
    }

    void startStopPk(long reqId, String myRoomId, String targetRoomId) {
        mLiveRoomService.startStopPk(reqId, Request.PK_START_STOP, myRoomId, targetRoomId);
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
                GiftListResponse giftListResponse = mGson.fromJson(json, GiftListResponse.class);
                break;
            case Request.MUSIC_LIST:
                MusicListResponse musicListResponse = mGson.fromJson(json, MusicListResponse.class);
                break;
            case Request.OSS:
                OssPolicyResponse ossPolicyResponse = mGson.fromJson(json, OssPolicyResponse.class);
                break;
            case Request.CREATE_USER:
                CreateUserResponse createUserResponse = mGson.fromJson(json, CreateUserResponse.class);
                break;
            case Request.EDIT_USER:
                EditUserResponse editUserResponse = mGson.fromJson(json, EditUserResponse.class);
                break;
            case Request.ROOM_LIST:
                RoomListResponse roomListResponse = mGson.fromJson(json, RoomListResponse.class);
                break;
            case Request.CREATE_ROOM:
                CreateRoomResponse createRoomResponse = mGson.fromJson(json, CreateRoomResponse.class);
                break;
            case Request.ENTER_ROOM:
                EnterRoomResponse enterRoomResponse = mGson.fromJson(json, EnterRoomResponse.class);
                break;
            case Request.LEAVE_ROOM:
                LeaveRoomResponse leaveRoomResponse = mGson.fromJson(json, LeaveRoomResponse.class);
                break;
            case Request.AUDIENCE_LIST:
                AudienceListResponse audienceListResponse = mGson.fromJson(json, AudienceListResponse.class);
                break;
            case Request.SEND_GIFT:
                SendGiftResponse sendGiftResponse = mGson.fromJson(json, SendGiftResponse.class);
                break;
            case Request.GIFT_RANK:
                GiftRankResponse giftRankResponse = mGson.fromJson(json, GiftRankResponse.class);
                break;
            case Request.SEAT_STATE:
                SeatStateResponse seatStateResponse = mGson.fromJson(json, SeatStateResponse.class);
                break;
            case Request.MODIFY_SEAT_STATE:
                ModifySeatStateResponse modifySeatStateResponse = mGson.fromJson(json, ModifySeatStateResponse.class);
                break;
            case Request.REFRESH_TOKEN:
                RefreshTokenResponse refreshTokenResponse = mGson.fromJson(json, RefreshTokenResponse.class);
                break;
            case Request.PK_START_STOP:
                StartStopPkResponse pkResponse = mGson.fromJson(json, StartStopPkResponse.class);
                break;
        }
    }

    @Override
    public void onFailure(@NonNull Call<ResponseBody> call, Throwable t) {
        t.printStackTrace();
    }
}
