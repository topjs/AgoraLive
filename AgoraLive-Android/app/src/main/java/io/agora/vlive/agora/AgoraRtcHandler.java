package io.agora.vlive.agora;

import java.util.ArrayList;
import java.util.List;

import io.agora.rtc.IRtcEngineEventHandler;

public class AgoraRtcHandler extends IRtcEngineEventHandler {
    private List<EventHandler> mHandlers;

    AgoraRtcHandler() {
        mHandlers = new ArrayList<>();
    }

    public void registerEventHandler(EventHandler handler) {
        if (!mHandlers.contains(handler)) {
            mHandlers.add(handler);
        }
    }

    public void removeEventHandler(EventHandler handler) {
        mHandlers.remove(handler);
    }
}
