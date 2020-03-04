package io.agora.vlive.proxy.struts.response;

import java.util.List;

public class GiftRankResponse extends AbsResponse {
    public List<GiftInfo> data;

    public class GiftInfo {
        public String userId;
        public String userName;
        public String avator;
    }
}
