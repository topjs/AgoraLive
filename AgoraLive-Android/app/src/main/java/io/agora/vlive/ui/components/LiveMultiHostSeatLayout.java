package io.agora.vlive.ui.components;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import java.util.ArrayList;
import java.util.List;

import io.agora.vlive.R;
import io.agora.vlive.proxy.struts.model.SeatInfo;

public class LiveMultiHostSeatLayout extends RelativeLayout {
    private static final int MAX_SEAT = 6;

    public interface LiveHostInSeatOnClickedListener {
        /**
         * Called when the owner clicks the "+" icon and whats
         * to select an audience to be the host.
         * @param position seat position
         * @param view the view clicked
         */
        void onSeatAdapterHostInviteClicked(int position, View view);

        /**
         * Called when the audience clicks the "+" icon of a seat
         * and tells the owner that he wants to be a host
         * @param position seat position
         * @param view the view clicked
         */
        void onSeatAdapterAudienceApplyClicked(int position, View view);

        /**
         * Called when the owner wants to close a seat and
         * no one can be a host of this seat
         * @param position seat position
         * @param view the view clicked
         */
        void onSeatAdapterPositionClosed(int position, View view);

        /**
         * Called when the "more" button is clicked for other
         * operations of this seat.
         * @param position the seat position
         * @param view the "more" button of this seat
         * @param seatState current seat states (open, taken or closed)
         * @param audioMuteState if the seat is taken, the audio mute state of
         *                  current user
         */
        void onSeatAdapterMoreClicked(int position, View view, int seatState, int audioMuteState);

        /** Called when the video of a seat position needs to be showed.
         /* The callback method needs to return a SurfaceView.
         * @param position seat position
         * @param uid the rtc uid of the user in this seat
         **/
        SurfaceView onSeatAdapterItemVideoShowed(int position, int uid);

        /**
         * Called when the video of a seat position is about to be removed.
         * @param position seat position
         * @param view the video view of this seat
         */
        void onSeatAdapterItemVideoRemoved(int position, SurfaceView view);
    }

    private static class SeatItem {
        RelativeLayout layout;
        FrameLayout videoLayout;
        AppCompatImageView operationIcon;
        AppCompatTextView operationText;
        AppCompatTextView nickname;
        AppCompatImageView popup;
        AppCompatImageView voiceState;
        SurfaceView surfaceView;
        int position;
        int seatState;
        int audioMuteState;
        int videoMuteState;

        int rtcUid;
        String userName;
        String userId;
    }

    public static final int SEAT_OPEN = 0;
    public static final int SEAT_TAKEN = 1;
    public static final int SEAT_CLOSED = 2;

    public static final int MUTE_NONE = 0;
    public static final int AUDIO_MUTED_BY_ME = 1;
    public static final int AUDIO_MUTED_BY_OWNER = 2;
    public static final int VIDEO_MUTED = 3;

    private List<SeatItem> mSeatList;
    private boolean mIsOwner;
    private boolean mIsHost;
    private String mMyUserId;
    private LiveHostInSeatOnClickedListener mListener;

    public LiveMultiHostSeatLayout(Context context) {
        super(context);
        init();
    }

    public LiveMultiHostSeatLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.live_host_in_seat_layout, this, true);

        mSeatList = new ArrayList<>(MAX_SEAT);
        mSeatList.add(getItemLayout(findViewById(R.id.live_host_seat_item_1), 0, "1"));
        mSeatList.add(getItemLayout(findViewById(R.id.live_host_seat_item_2), 1, "2"));
        mSeatList.add(getItemLayout(findViewById(R.id.live_host_seat_item_3), 2, "3"));
        mSeatList.add(getItemLayout(findViewById(R.id.live_host_seat_item_4), 3, "4"));
        mSeatList.add(getItemLayout(findViewById(R.id.live_host_seat_item_5), 4, "5"));
        mSeatList.add(getItemLayout(findViewById(R.id.live_host_seat_item_6), 5, "6"));
        initSeats();
    }

    private SeatItem getItemLayout(RelativeLayout layout, int position, String id) {
        AppCompatTextView idText = layout.findViewById(R.id.host_in_seat_item_id);
        idText.setText(id);

        final SeatItem item = new SeatItem();
        item.layout = layout;
        item.position = position;
        item.videoLayout = layout.findViewById(R.id.host_in_seat_item_video_layout);
        item.operationIcon = layout.findViewById(R.id.seat_item_operation_icon);
        item.operationText = layout.findViewById(R.id.seat_item_operation_text);
        item.nickname = layout.findViewById(R.id.host_in_seat_item_nickname);
        item.popup = layout.findViewById(R.id.seat_item_owner_popup_btn);
        item.voiceState = layout.findViewById(R.id.host_in_seat_item_voice_state_icon);

        item.operationIcon.setOnClickListener(view -> {
            if (item.seatState == SEAT_OPEN && mListener != null) {
                if (mIsOwner) {
                    mListener.onSeatAdapterHostInviteClicked(item.position, view);
                } else {
                    mListener.onSeatAdapterAudienceApplyClicked(item.position, view);
                }
            } else if (item.seatState == SEAT_CLOSED && mListener != null) {
                mListener.onSeatAdapterPositionClosed(item.position, view);
            }
        });

        item.popup.setOnClickListener(view -> {
            if (mListener != null) {
                mListener.onSeatAdapterMoreClicked(item.position, view, item.seatState, item.audioMuteState);
            }
        });

        return item;
    }

    private void initSeats() {
        for (SeatItem item : mSeatList) {
            item.seatState = SEAT_OPEN;
            item.audioMuteState = MUTE_NONE;
            item.videoMuteState = MUTE_NONE;
        }
        updateSeatStates(null);
    }

    public void setOwner(boolean isOwner) {
        mIsOwner = isOwner;
    }

    public void setHost(boolean isHost) {
        mIsHost = isHost;
    }

    public void setMyUserId(String userId) {
        mMyUserId = userId;
    }

    public void setSeatListener(LiveHostInSeatOnClickedListener listener) {
        mListener = listener;
    }

    /**
     * Update seat states and UI
     * @param list seat info list from the response of server, null
     *             to updates seat UI according to current seat states
     */
    public void updateSeatStates(List<SeatInfo> list) {
        for (int i = 0; i < MAX_SEAT; i++) {
            SeatItem curInfo = mSeatList.get(i);
            int seatStateNow = curInfo.seatState;
            int audioMuted = curInfo.audioMuteState;
            int videoMuted = curInfo.videoMuteState;

            int newSeatState = seatStateNow;
            int newAudioMuted = audioMuted;
            int newVideoMuted = videoMuted;

            SeatInfo info = null;
            if (list != null && list.size() > i) {
                info = list.get(i);
                if (info != null) {
                    newSeatState = info.seat.state;
                    newAudioMuted = info.user.enableAudio > 0 ? AUDIO_MUTED_BY_ME : MUTE_NONE;
                    newVideoMuted = info.user.enableVideo > 0 ? VIDEO_MUTED : MUTE_NONE;
                }
            }

            if (seatStateNow != newSeatState) {
                if (newSeatState == SEAT_TAKEN) {
                    curInfo.rtcUid = info.user.uid;
                    curInfo.userName = info.user.userName;
                    curInfo.userId = info.user.userId;
                    curInfo.seatState = newSeatState;
                    curInfo.audioMuteState = newAudioMuted;
                    curInfo.videoMuteState = newVideoMuted;
                    updateItemUI(curInfo);
                    if (mListener != null) curInfo.surfaceView = mListener.
                            onSeatAdapterItemVideoShowed(curInfo.position, curInfo.rtcUid);
                    curInfo.videoLayout.addView(curInfo.surfaceView);
                } else if (seatStateNow == SEAT_TAKEN) {
                    curInfo.rtcUid = 0;
                    curInfo.userName = null;
                    curInfo.userId = null;
                    curInfo.seatState = newSeatState;
                    curInfo.audioMuteState = newAudioMuted;
                    curInfo.videoMuteState = newVideoMuted;
                    updateItemUI(curInfo);
                    if (mListener != null) mListener.
                            onSeatAdapterItemVideoRemoved(curInfo.position, curInfo.surfaceView);
                    curInfo.videoLayout.removeView(curInfo.surfaceView);
                    curInfo.surfaceView = null;
                }
            } else {
                updateItemUI(curInfo);
            }
        }
    }

    private void updateItemUI(SeatItem item) {
        if (item.seatState == SEAT_TAKEN) {
            item.nickname.setVisibility(VISIBLE);
            item.videoLayout.setVisibility(VISIBLE);
            item.voiceState.setVisibility(VISIBLE);
            item.operationIcon.setVisibility(GONE);
            item.operationText.setVisibility(GONE);

            if (item.audioMuteState == SeatInfo.User.USER_AUDIO_MUTED) {
                item.voiceState.setImageResource(R.drawable.host_seat_item_voice_off);
            } else if (item.audioMuteState == SeatInfo.User.OWNER_AUDIO_MUTED) {
                item.voiceState.setImageResource(R.drawable.host_seat_item_mute_icon);
            } else if (item.audioMuteState == SeatInfo.User.USER_AUDIO_ENABLE) {
                item.voiceState.setImageResource(R.drawable.host_seat_item_voice_anim);
            }
        } else {
            item.nickname.setVisibility(View.GONE);
            item.videoLayout.removeAllViews();
            item.videoLayout.setVisibility(GONE);
            item.voiceState.setVisibility(GONE);
            item.operationIcon.setVisibility(View.VISIBLE);

            if (item.seatState == SEAT_OPEN) {
                item.operationIcon.setImageResource(R.drawable.live_seat_invite);
                item.operationText.setVisibility(VISIBLE);
                item.operationText.setText(mIsOwner ?
                        R.string.live_host_in_seat_state_open_host :
                        R.string.live_host_in_seat_state_open_audience);
            } else if (item.seatState == SEAT_CLOSED) {
                item.operationIcon.setImageResource(R.drawable.live_seat_close);
                item.operationText.setVisibility(GONE);
            }
        }

        if (mIsOwner || mIsHost && mMyUserId != null && mMyUserId.equals(item.userId)) {
            item.popup.setVisibility(VISIBLE);
        }
    }
}
