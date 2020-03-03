package io.agora.vlive.ui.main.fragments;

import androidx.fragment.app.Fragment;

import io.agora.vlive.AgoraLiveApplication;
import io.agora.vlive.ui.main.MainActivity;

public class AbstractFragment extends Fragment {
    protected AgoraLiveApplication application() {
        return (AgoraLiveApplication) getContext().getApplicationContext();
    }

    public MainActivity getContainer() {
        return (MainActivity) getActivity();
    }
}