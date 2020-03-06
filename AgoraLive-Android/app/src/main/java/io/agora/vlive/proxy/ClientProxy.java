package io.agora.vlive.proxy;

import android.util.SparseArray;

import io.agora.vlive.proxy.struts.request.CreateRoomRequest;
import io.agora.vlive.proxy.struts.request.ModifySeatStateRequest;
import io.agora.vlive.proxy.struts.request.PKRequest;
import io.agora.vlive.proxy.struts.request.RoomRequest;
import io.agora.vlive.proxy.struts.request.RoomListRequest;
import io.agora.vlive.proxy.struts.request.SendGiftRequest;
import io.agora.vlive.proxy.struts.request.UserRequest;
import io.agora.vlive.proxy.struts.request.OssPolicyRequest;
import io.agora.vlive.proxy.struts.request.Request;
import retrofit2.Call;

public class ClientProxy {
    public static final int ROOM_TYPE_SINGLE = 1;
    public static final int ROOM_TYPE_HOST_IN = 2;
    public static final int ROOM_TYPE_PK = 3;

    public static final int PK_WAIT = 0;
    public static final int PK_IN = 1;
    public static final int PK_UNAWARE = 2;

    private static final String APP_CODE = "ent-super";
    private static final int OS_TYPE = 2;

    // 1 means android phone app (rather than a pad app)
    private static final int TERMINAL_TYPE = 1;

    private Client mClient;
    private long mReqId = 1;
    private SparseArray<Call> mReqMap;

    public ClientProxy() {
        mClient = Client.instance();
        mReqMap = new SparseArray<>();
    }

    public long sendReq(int request, Object params, ClientProxyListener listener) {
        switch (request) {
            case Request.APP_VERSION:
                String ver = (String) params;
                mClient.requestVersion(mReqId, APP_CODE, OS_TYPE, TERMINAL_TYPE, ver);
                break;
            case Request.GIFT_LIST:
                mClient.requestGiftList(mReqId);
                break;
            case Request.MUSIC_LIST:
                mClient.requestMusicList(mReqId);
                break;
            case Request.OSS:
                OssPolicyRequest ossRequest = (OssPolicyRequest) params;
                mClient.requestOssPolicy(mReqId, ossRequest.token, ossRequest.type);
                break;
            case Request.CREATE_USER:
                UserRequest userRequest = (UserRequest) params;
                mClient.createUser(mReqId, userRequest.userName, userRequest.avatar);
                break;
            case Request.EDIT_USER:
                userRequest = (UserRequest) params;
                mClient.editUser(mReqId, userRequest.token, userRequest.userName, userRequest.avatar);
                break;
            case Request.ROOM_LIST:
                RoomListRequest roomListRequest = (RoomListRequest) params;
                mClient.requestRoomList(mReqId, roomListRequest.nextId,
                        roomListRequest.count, roomListRequest.type, roomListRequest.pkState);
                break;
            case Request.CREATE_ROOM:
                CreateRoomRequest createRoomRequest = (CreateRoomRequest) params;
                mClient.createRoom(mReqId, createRoomRequest.token,
                        createRoomRequest.userName, createRoomRequest.type);
                break;
            case Request.ENTER_ROOM:
                RoomRequest roomRequest = (RoomRequest) params;
                mClient.enterRoom(mReqId, roomRequest.token, roomRequest.roomId);
                break;
            case Request.LEAVE_ROOM:
                roomRequest = (RoomRequest) params;
                mClient.leaveRoom(mReqId, roomRequest.token, roomRequest.roomId);
                break;
            case Request.AUDIENCE_LIST:
                roomRequest = (RoomRequest) params;
                mClient.requestAudienceList(mReqId, roomRequest.token, roomRequest.roomId);
                break;
            case Request.SEND_GIFT:
                SendGiftRequest sendGiftRequest = (SendGiftRequest) params;
                mClient.sendGift(mReqId, sendGiftRequest.roomId,
                        sendGiftRequest.giftId, sendGiftRequest.count);
                break;
            case Request.GIFT_RANK:
                String roomId = (String) params;
                mClient.giftRank(mReqId, roomId);
                break;
            case Request.SEAT_STATE:
                roomRequest = (RoomRequest) params;
                mClient.requestSeatState(mReqId, roomRequest.token, roomRequest.roomId);
                break;
            case Request.MODIFY_SEAT_STATE:
                ModifySeatStateRequest modifySeatRequest = (ModifySeatStateRequest) params;
                mClient.modifySeatState(mReqId, modifySeatRequest.token, modifySeatRequest.roomId,
                        modifySeatRequest.no, modifySeatRequest.userId, modifySeatRequest.state);
                break;
            case Request.REFRESH_TOKEN:
                roomId = (String) params;
                mClient.refreshToken(mReqId, roomId);
                break;
            case Request.PK_START_STOP:
                PKRequest pkRequest = (PKRequest) params;
                mClient.startStopPk(mReqId, pkRequest.myRoomId, pkRequest.targetRoomId);
                break;
        }

        mClient.setProxyListener(mReqId, listener);
        return mReqId++;
    }

    public void removeListener(long reqId) {
        mClient.removeProxyListener(reqId);
    }
}
