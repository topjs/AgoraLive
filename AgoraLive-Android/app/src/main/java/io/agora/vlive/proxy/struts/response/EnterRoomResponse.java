package io.agora.vlive.proxy.struts.response;

import java.util.List;

public class EnterRoomResponse extends AbsResponse {
    public RoomData data;

    public class RoomData {
        public RoomInfo room;
        public OwnerInfo user;
    }

    public class RoomInfo {
        public String roomId;
        public String roomName;
        public String channelName;
        public int type;
        public List<SeatInfo> coVideoSeats;
    }

    public class SeatInfo {
        public int no;
        public String userId;
        public String userName;
        public int uid;
        public int state;
        public List<String> rankUsers;
        public String pkRoomId;
        public long pkStartTime;
        public int roomRank;
        public int ownerUid;
    }

    public class OwnerInfo {
        public String userId;
        public String userName;
        public String avator;
        public int role;
        public int uid;
        public String rtcToken;
        public String rtmToken;
        public int coVideo;
        public int enableChat;
        public int enableVideo;
        public int enableAudio;
        public int rank;
    }
}
