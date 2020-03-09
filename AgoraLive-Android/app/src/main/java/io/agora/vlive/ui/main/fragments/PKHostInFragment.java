package io.agora.vlive.ui.main.fragments;

import io.agora.vlive.ui.live.HostPKLiveActivity;
import io.agora.vlive.utils.Global;

public class PKHostInFragment extends AbsPageFragment {
    @Override
    protected int onGetTabType() {
        return Global.Constants.TAB_ID_PK;
    }

    @Override
    protected Class<?> getLiveActivityClass() {
        return HostPKLiveActivity.class;
    }
}
