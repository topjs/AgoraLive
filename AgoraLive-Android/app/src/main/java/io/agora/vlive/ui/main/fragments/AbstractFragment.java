package io.agora.vlive.ui.main.fragments;

import androidx.fragment.app.Fragment;

import io.agora.vlive.AgoraLiveApplication;

public class AbstractFragment extends Fragment {
    protected AgoraLiveApplication application() {
        return (AgoraLiveApplication) getContext().getApplicationContext();
    }
}