package io.agora.vlive.ui.main.fragments;

import io.agora.vlive.utils.Global;

public class SingleHostFragment extends AbsPageFragment {
    @Override
    protected int onGetTabType() {
        return Global.Constants.TAB_ID_SINGLE;
    }
}
