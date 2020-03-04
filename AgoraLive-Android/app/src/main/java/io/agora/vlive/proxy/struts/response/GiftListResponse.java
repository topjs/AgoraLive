package io.agora.vlive.proxy.struts.response;

import java.util.List;

public class GiftListResponse extends AbsResponse {
    public List<Gift> data;

    public class Gift {
        public String giftId;
        public String giftName;
        public String thumnail;
        public int points;
    }
}