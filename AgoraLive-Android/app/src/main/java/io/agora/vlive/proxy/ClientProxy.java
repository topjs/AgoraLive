package io.agora.vlive.proxy;

import android.util.SparseArray;

import io.agora.vlive.proxy.struts.request.Request;
import retrofit2.Call;

public class ClientProxy implements ClientProxyListener {
    private static final String APP_CODE = "ent-super";
    private static final int OS_TYPE = 2;

    // 1 means android phone app (rather than a pad app)
    private static final int TERMINAL_TYPE = 1;

    private Client mClient;
    private long mReqId = 1;
    private SparseArray<Call> mReqMap;

    public ClientProxy() {
        mClient = Client.instance();
        mClient.setProxyListener(this);
        mReqMap = new SparseArray<>();
    }

    public long sendReq(int request, Object params) {
        switch (request) {
            case Request.APP_VERSION:
                String ver = (String) params;
                mClient.getAppVersion(mReqId, APP_CODE, OS_TYPE, TERMINAL_TYPE, ver);
                break;
            case Request.GIFT_LIST:
                mClient.getGiftList(mReqId);
                break;
            case Request.MUSIC_LIST:
                mClient.getMusicList(mReqId);
                break;
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

        return mReqId++;
    }
}
