package io.agora.vlive.agora;

import java.util.ArrayList;
import java.util.List;

import io.agora.rtc.IRtcEngineEventHandler;

public class AgoraRtcHandler extends IRtcEngineEventHandler {
    private List<RtcEventHandler> mHandlers;

    AgoraRtcHandler() {
        mHandlers = new ArrayList<>();
    }

    public void registerEventHandler(RtcEventHandler handler) {
        if (!mHandlers.contains(handler)) {
            mHandlers.add(handler);
        }
    }

    public void removeEventHandler(RtcEventHandler handler) {
        mHandlers.remove(handler);
    }
}
