package io.agora.vlive.proxy.struts.request;

public class ModifyUserStateRequest extends RoomRequest {
    public String userId;
    public int enableAudio;
    public int enableVideo;
    public int enableChat;
}
