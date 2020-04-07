package io.agora.vlive.proxy.struts.response;

import java.util.List;

import io.agora.vlive.proxy.struts.model.EnterRoomUserInfo;
import io.agora.vlive.proxy.struts.model.SeatInfo;

public class EnterRoomResponse extends AbsResponse {
    public RoomData data;

    public static class RoomData {
        public RoomInfo room;
        public EnterRoomUserInfo user;
    }

    public static class RoomInfo {
        public String roomId;
        public String roomName;
        public String channelName;
        public int type;
        public List<SeatInfo> coVideoSeats;
        public List<RankInfo> rankUsers;
        public String pkRoomId;
        public long pkStartTime;
        public int roomRank;
        public int total;
        public Owner owner;
    }

    public static class RankInfo {
        public String userId;
        public String userName;
        public String avatar;
    }

    public static class Owner {
        public String userId;
        public int uid;
        public String userName;
        public int enableVideo;
        public int enableAudio;
    }
}
