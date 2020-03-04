package io.agora.vlive.proxy.struts.request;

public class OssPolicyRequest {
    public static final int OSS_TYPE_AVATOR = 1;

    public OssPolicyRequest(String token) {
        this.token = token;
    }

    public String token;
    public int type = OSS_TYPE_AVATOR;
}
