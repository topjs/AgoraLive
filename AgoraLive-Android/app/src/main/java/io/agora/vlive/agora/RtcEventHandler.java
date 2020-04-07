package io.agora.vlive.agora;

public interface RtcEventHandler {
    void onRtcJoinChannelSuccess(String channel, int uid, int elapsed);
}
