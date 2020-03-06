package io.agora.vlive.ui.main.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import io.agora.vlive.R;

public class SingleHostFragment extends AbsPageFragment {
    private RecyclerView mRecycler;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_single_host, container, false);
        mRecycler = layout.findViewById(R.id.single_host_room_list_recycler);
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(),
                2, RecyclerView.VERTICAL,false);
        mRecycler.setLayoutManager(layoutManager);
        return layout;
    }

    @Override
    void onPeriodicRefreshTimerTicked() {

    }
}
