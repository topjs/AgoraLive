package io.agora.vlive.ui.main.fragments;

import io.agora.vlive.proxy.ClientProxy;

public class VirtualHostFragment extends AbsPageFragment {
    @Override
    protected int onGetRoomListType() {
        return ClientProxy.ROOM_TYPE_VIRTUAL_HOST;
    }

    @Override
    protected Class<?> getLiveActivityClass() {
        return null;
    }
}
