package io.agora.vlive.proxy.struts.response;

import java.util.List;

public class RoomListResponse extends AbsResponse {
    public RoomList data;

    public class RoomList {
        public int count;
        public String total;
        public String next;
        List<RoomInfo> list;
    }

    public class RoomInfo {
        public String roomId;
        public String roomName;
        public String thumnbail;
        public int currentUsers;
        public int ownerUid;
    }
}
