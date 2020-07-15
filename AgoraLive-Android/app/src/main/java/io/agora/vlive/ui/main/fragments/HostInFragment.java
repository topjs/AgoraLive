package io.agora.vlive.ui.main.fragments;

import io.agora.vlive.proxy.ClientProxy;
import io.agora.vlive.ui.live.MultiHostLiveActivity;

public class HostInFragment extends AbsPageFragment {
    @Override
    protected int onGetRoomListType() {
        return ClientProxy.ROOM_TYPE_HOST_IN;
    }

    @Override
    protected Class<?> getLiveActivityClass() {
        return MultiHostLiveActivity.class;
    }
}
