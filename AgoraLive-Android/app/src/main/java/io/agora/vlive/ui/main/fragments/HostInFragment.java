package io.agora.vlive.ui.main.fragments;

import io.agora.vlive.ui.live.HostInLiveActivity;
import io.agora.vlive.utils.Global;

public class HostInFragment extends AbsPageFragment {
    @Override
    protected int onGetTabType() {
        return Global.Constants.TAB_ID_MULTI;
    }

    @Override
    protected Class<?> getLiveActivityClass() {
        return HostInLiveActivity.class;
    }
}
