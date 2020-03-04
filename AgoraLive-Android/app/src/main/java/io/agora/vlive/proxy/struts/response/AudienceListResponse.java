package io.agora.vlive.proxy.struts.response;

import java.util.List;

public class AudienceListResponse extends AbsResponse {
    List<AudienceInfo> data;

    public class AudienceInfo {
        public String userId;
        public String userName;
        public String avator;
        public int uid;
    }
}
