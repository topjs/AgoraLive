package io.agora.vlive.ui.main.fragments;

import io.agora.vlive.proxy.ClientProxy;
import io.agora.vlive.ui.live.HostInLiveActivity;
import io.agora.vlive.utils.Global;

public class HostInFragment extends AbsPageFragment {
    @Override
    protected int onGetRoomListType() {
        return ClientProxy.ROOM_TYPE_HOST_IN;
    }

    @Override
    protected Class<?> getLiveActivityClass() {
        return HostInLiveActivity.class;
    }
}
