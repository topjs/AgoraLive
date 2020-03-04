package io.agora.vlive.proxy.struts.response;

import java.util.List;

public class MusicListResponse extends AbsResponse {
    public List<MusicInfo> data;

    public class MusicInfo {
        String musicid;
        String musicName;
        String url;
    }
}
