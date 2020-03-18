package io.agora.vlive.proxy.struts.response;

import java.util.List;

import io.agora.vlive.proxy.model.EnterRoomUserInfo;
import io.agora.vlive.proxy.model.SeatInfo;

public class EnterRoomResponse extends AbsResponse {
    public RoomData data;

    public class RoomData {
        public RoomInfo room;
        public EnterRoomUserInfo user;
    }

    public class RoomInfo {
        public String roomId;
        public String roomName;
        public String channelName;
        public int type;
        public List<SeatInfo> coVideoSeats;
        public String pkRoomId;
        public long pkStartTime;
        public int ownerUid;
        List<String> rankUsers;
    }
}
