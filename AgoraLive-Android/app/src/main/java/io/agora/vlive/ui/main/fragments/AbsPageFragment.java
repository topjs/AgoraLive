package io.agora.vlive.ui.main.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;

import io.agora.vlive.proxy.struts.request.Request;
import io.agora.vlive.proxy.struts.request.RoomListRequest;

public abstract class AbsPageFragment extends AbstractFragment {
    protected static final int SPAN_COUNT = 2;

    // By default, the client asks for 10 more rooms to show in the list
    protected static final int REQ_ROOM_COUNT = 10;
    private static final int REFRESH_DELAY = 1000 * 60;

    long mLastRefreshReq = -1;

    private Handler mHandler;
    private PageRefreshRunnable mPageRefreshRunnable;
    private boolean mGlobalRefresh;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new Handler(Looper.getMainLooper());
        mPageRefreshRunnable = new PageRefreshRunnable();
    }

    void startRefreshTimer() {
        mHandler.postDelayed(mPageRefreshRunnable, REFRESH_DELAY);
    }

    void stopRefreshTimer() {
        mHandler.removeCallbacks(mPageRefreshRunnable);
    }

    private class PageRefreshRunnable implements Runnable {
        @Override
        public void run() {
            onPeriodicRefreshTimerTicked();
            mHandler.postDelayed(mPageRefreshRunnable, REFRESH_DELAY);
        }
    }

    abstract void onPeriodicRefreshTimerTicked();

    @Override
    public void onResume() {
        super.onResume();
        startRefreshTimer();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopRefreshTimer();
    }

    protected void refreshPage(String nextId, int count, int type, int pkState) {
        RoomListRequest request = new RoomListRequest();
        request.nextId = nextId;
        request.count = count;
        request.type = type;
        request.pkState = pkState;
        mLastRefreshReq = getContainer().proxy().sendReq(Request.ROOM_LIST, request, this);
    }
}
