package io.agora.vlive.ui.main.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import io.agora.vlive.R;
import io.agora.vlive.proxy.ClientProxy;
import io.agora.vlive.proxy.struts.response.RoomListResponse;

public class HostInFragment extends AbsPageFragment implements SwipeRefreshLayout.OnRefreshListener {
    private static final String TAG = HostInFragment.class.getSimpleName();

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_host_in, container, false);
        mSwipeRefreshLayout = layout.findViewById(R.id.host_in_swipe);
        mSwipeRefreshLayout.setNestedScrollingEnabled(true);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        mRecyclerView = layout.findViewById(R.id.host_in_room_list_recycler);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), SPAN_COUNT));
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                Log.i(TAG, "page scroll state:" + newState);
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    stopRefreshTimer();
                } else if (newState == RecyclerView.SCROLL_STATE_IDLE) {

                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
        return layout;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh the room list every time we come to this page.
        refreshPage(null, REQ_ROOM_COUNT, ClientProxy.ROOM_TYPE_HOST_IN, ClientProxy.PK_UNAWARE);
    }

    @Override
    void onPeriodicRefreshTimerTicked() {
        Log.i(TAG, "onPeriodicRefreshTimerTicked");
    }

    @Override
    public void onRefresh() {
        Log.i(TAG, "onPageRefresh");
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onRoomListResponse(RoomListResponse response) {
        Log.i(TAG, "onRoomListResponse:" + mLastRefreshReq);
        getContainer().proxy().removeListener(mLastRefreshReq);
        mLastRefreshReq = -1;
    }
}
