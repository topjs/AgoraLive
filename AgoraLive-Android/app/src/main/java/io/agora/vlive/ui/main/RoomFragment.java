package io.agora.vlive.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import io.agora.vlive.Global;

public class RoomFragment extends Fragment {
    private static final int DEFAULT_DESTINATION = Global.Constants.TAB_ID_MULTI;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        int dest = bundle == null ? DEFAULT_DESTINATION : bundle.getInt(Global.Constants.TAB_KEY);
        return super.onCreateView(inflater, container, savedInstanceState);
    }


}
