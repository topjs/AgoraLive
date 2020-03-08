package io.agora.vlive.proxy.struts.response;

import java.util.List;

import io.agora.vlive.proxy.model.RoomInfo;

public class RoomListResponse extends AbsResponse {
    public RoomList data;

    public class RoomList {
        public int count;
        public String total;
        public String next;
        public List<RoomInfo> list;
    }
}
