package io.agora.vlive.ui.components;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import io.agora.vlive.R;
import io.agora.vlive.proxy.model.SeatInfo;

public class LiveHostInSeatAdapter extends RecyclerView.Adapter {
    public interface LiveHostInSeatOnClickedListener {
        void onSeatAdapterHostInviteClicked(int position, View view);
        void onSeatAdapterAudienceApplyClicked(int position, View view);
        void onSeatAdapterPositionClosed(int position, View view);
        void onSeatAdapterMoreClicked(int position, View view, SeatState state);
    }

    private static final int MAX_SEAT = 6;

    private LayoutInflater mInflater;
    private ArrayList<Seat> mSeatList;
    private ArrayList<AppCompatImageView> mMuteIcons;
    private ArrayList<AppCompatImageView> mUserBg;
    private ArrayList<FrameLayout> mVideoLayouts;
    private ArrayList<SeatViewHolder> mViewHolders;
    private boolean mIsRoomOwner;

    private LiveHostInSeatOnClickedListener mListener;

    public LiveHostInSeatAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
        initSeatList();
        initViewRecords();
    }

    private void initSeatList() {
        mSeatList = new ArrayList<>(MAX_SEAT);
        for (int i = 0; i < MAX_SEAT; i++) {
            Seat seat = new Seat();
            seat.seatNo = i + 1;
            seat.state.setOpen();
            mSeatList.add(seat);
        }
    }

    public void resetSeatStates(List<SeatInfo> list) {
        for (SeatInfo info : list) {
            // Seat no starts from 1
            Seat seat = mSeatList.get(info.no - 1);
            if (info.state == SeatInfo.SPEAK ||
                info.state == SeatInfo.MUTED) {
                seat.userId = info.userId;
                seat.nickname = info.userName;
                seat.uid = info.uid;
            }

            switch (info.state) {
                case SeatInfo.OPEN:
                    seat.state.setOpen();
                    break;
                case SeatInfo.CLOSE:
                    seat.state.setClosed();
                    break;
                case SeatInfo.SPEAK:
                    seat.state.setSpeaking();
                    break;
                case SeatInfo.MUTED:
                    seat.state.setAudioMutedByOwner();
                    break;
            }
        }
    }

    private void initViewRecords() {
        mMuteIcons = new ArrayList<>(MAX_SEAT);
        mUserBg = new ArrayList<>(MAX_SEAT);
        mVideoLayouts = new ArrayList<>(MAX_SEAT);
        mViewHolders = new ArrayList<>(MAX_SEAT);
    }

    public void setIsRoomOwner(boolean isOwner) {
        mIsRoomOwner = isOwner;
    }

    public void setSeatListener(LiveHostInSeatOnClickedListener listener) {
        mListener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SeatViewHolder(mInflater.inflate(
                R.layout.live_host_in_seat_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        setViewState((SeatViewHolder) holder, position);
    }

    private void setViewState(SeatViewHolder holder, int position) {
        Seat seat = mSeatList.get(position);
        attachComponents(position, holder);

        holder.id.setText(String.valueOf(seat.seatNo));
        holder.muteIcon.setVisibility(View.GONE);

        if (seat.state.isOpen()) {
            holder.operateIcon.setImageResource(R.drawable.live_seat_invite);
            int textRes = mIsRoomOwner ?
                    R.string.live_host_in_seat_state_open_host :
                    R.string.live_host_in_seat_state_open_audience;
            holder.operateText.setText(textRes);
            holder.more.setVisibility(View.GONE);
        } else if (seat.state.isClosed()) {
            holder.operateText.setText(R.string.live_host_in_seat_state_closed);
            holder.operateIcon.setImageResource(R.drawable.live_seat_close);
            holder.more.setVisibility(View.GONE);
        } else if (seat.state.isTaken()) {
            if (mIsRoomOwner) {
                holder.more.setVisibility(View.VISIBLE);
                holder.more.setOnClickListener(v -> {
                    if (mListener != null) {
                        mListener.onSeatAdapterMoreClicked(position, v, seat.state);
                    }
                });
            }

            if (seat.state.isAudioMutedByOwner() ||
                    seat.state.isAudioMutedByMe()) {
                holder.muteIcon.setVisibility(View.VISIBLE);
            }

            if (seat.state.isVideoMutedByMe()) {
                holder.videoLayout.setVisibility(View.GONE);
                holder.background.setVisibility(View.VISIBLE);
            } else {
                holder.videoLayout.setVisibility(View.VISIBLE);
                holder.background.setVisibility(View.GONE);
            }
        }

        if (seat.state.isOpen() || seat.state.isClosed()) {
            final SeatState state = seat.state;
            holder.itemView.setOnClickListener(view -> {
                if (state.isOpen() && mListener != null) {
                    if (mIsRoomOwner) {
                        mListener.onSeatAdapterHostInviteClicked(position, view);
                    } else {
                        mListener.onSeatAdapterAudienceApplyClicked(position, view);
                    }
                } else if (state.isClosed() && mListener != null) {
                    mListener.onSeatAdapterPositionClosed(position, view);
                }
            });
        }

        //TODO for test
        holder.more.setVisibility(View.VISIBLE);
        holder.more.setOnClickListener(view -> {
            if (mListener != null) {
                mListener.onSeatAdapterMoreClicked(position, view, seat.state);
            }
        });
    }

    private void attachComponents(int position, SeatViewHolder holder) {
        AppCompatImageView icon = mMuteIcons.size() <= position ? null : mMuteIcons.get(position);
        if (icon == null || icon != holder.muteIcon) {
            mMuteIcons.add(position, holder.muteIcon);
        }

        AppCompatImageView bg = mUserBg.size() <= position ? null : mUserBg.get(position);
        if (bg == null || bg != holder.background) {
            mUserBg.add(position, holder.background);
        }

        FrameLayout layout = mVideoLayouts.size() <= position ? null : mVideoLayouts.get(position);
        if (layout == null || layout != holder.videoLayout) {
            mVideoLayouts.add(position, holder.videoLayout);
        }

        SeatViewHolder viewHolder = mViewHolders.size() <= position ? null : mViewHolders.get(position);
        if (viewHolder == null || viewHolder != holder) {
            mViewHolders.add(position, holder);
        }
    }

    public FrameLayout getVideoLayout(int position) {
        return mVideoLayouts.get(position);
    }

    @Override
    public int getItemCount() {
        return MAX_SEAT;
    }

    private class SeatViewHolder extends RecyclerView.ViewHolder {
        AppCompatTextView id;
        AppCompatTextView operateText;
        AppCompatImageView operateIcon;
        AppCompatImageView more;
        FrameLayout videoLayout;
        AppCompatImageView background;
        AppCompatTextView nickname;
        AppCompatImageView muteIcon;

        SeatViewHolder(@NonNull View itemView) {
            super(itemView);
            id = itemView.findViewById(R.id.host_in_seat_item_id);
            operateText = itemView.findViewById(R.id.seat_item_operation_text);
            operateIcon = itemView.findViewById(R.id.seat_item_operation_icon);
            more = itemView.findViewById(R.id.seat_item_owner_popup_btn);
            videoLayout = itemView.findViewById(R.id.host_in_seat_item_video_layout);
            background = itemView.findViewById(R.id.host_in_seat_item_bg);
            nickname = itemView.findViewById(R.id.host_in_seat_item_nickname);
            muteIcon = itemView.findViewById(R.id.host_in_seat_item_mute_icon);
        }
    }

    public void takeSeat(int position, String userId, String nickname, String avatarUrl) {
        Seat seat = mSeatList.get(position);
        seat.userId = userId;
        seat.nickname = nickname;
        seat.avatar = avatarUrl;
        seat.state.setTaken();
        seat.state.setSpeaking();
        SeatViewHolder holder = mViewHolders.get(position);
        setViewState(holder, position);
    }

    public void closeSeat(int position) {
        Seat seat = mSeatList.get(position);
        seat.userId = null;
        seat.nickname = null;
        seat.avatar = null;
        seat.state.setClosed();
        SeatViewHolder holder = mViewHolders.get(position);
        setViewState(holder, position);
    }

    public void leaveSeat(int position) {
        Seat seat = mSeatList.get(position);
        seat.userId = null;
        seat.nickname = null;
        seat.avatar = null;
        seat.state.setOpen();
        SeatViewHolder holder = mViewHolders.get(position);
        setViewState(holder, position);
    }

    public void muteAudio(int position, boolean muted, boolean byOwner) {
        Seat seat = mSeatList.get(position);
        if (byOwner) {
            if (muted) {
                seat.state.setAudioMutedByOwner();
            } else {
                seat.state.unmuteAudioByOwner();
            }
        } else {
            if (muted) {
                seat.state.setAudioMutedByMe();
            } else {
                seat.state.unmuteAudioByMe();
            }
        }

        if (muted) {
            mMuteIcons.get(position).setVisibility(View.VISIBLE);
        } else {
            mMuteIcons.get(position).setVisibility(View.GONE);
        }
    }

    public void muteVideo(int position, boolean muted) {
        Seat seat = mSeatList.get(position);
        if (muted) {
            seat.state.setVideoMutedByMe();
            mUserBg.get(position).setVisibility(View.VISIBLE);
            mVideoLayouts.get(position).setVisibility(View.GONE);
        } else {
            seat.state.unmuteVideoByMe();
            mUserBg.get(position).setVisibility(View.GONE);
            mVideoLayouts.get(position).setVisibility(View.VISIBLE);
        }
    }

    public static class Seat {
        int seatNo;
        int uid;
        String userId;
        String nickname;
        String avatar;
        SeatState state = new SeatState();

        public SeatState state() {
            return state;
        }
    }

    public static class SeatState {
        final int SEAT_TAKEN = 1;
        final int SEAT_OPEN = 1 << 1;
        final int SEAT_CLOSED = 1 << 2;
        final int SEAT_STATUS_MASK = SEAT_TAKEN | SEAT_OPEN | SEAT_CLOSED;

        final int SEAT_SPEAKING = 1 << 3;
        final int SEAT_MUTED_AUDIO_BY_OWNER = 1 << 4;
        final int SEAT_MUTED_AUDIO_BY_ME = 1 << 5;
        final int SEAT_MUTED_VIDEO_BY_ME = 1 << 6;
        final int SEAT_MUTE_MASK = SEAT_SPEAKING | SEAT_MUTED_AUDIO_BY_OWNER |
                SEAT_MUTED_AUDIO_BY_ME | SEAT_MUTED_VIDEO_BY_ME;

        int flags = 0;

        public void clear() {
            flags = 0;
        }

        public void setTaken() {
            flags = (flags & ~SEAT_STATUS_MASK) | SEAT_TAKEN;
        }

        public boolean isTaken() {
            return (flags & SEAT_TAKEN) != 0;
        }

        public void setOpen() {
            flags = (flags & ~SEAT_STATUS_MASK) | SEAT_OPEN;
        }

        public boolean isOpen() {
            return (flags & SEAT_OPEN) != 0;
        }

        public void setClosed() {
            flags = (flags & ~SEAT_STATUS_MASK) | SEAT_CLOSED;
        }

        public boolean isClosed() {
            return (flags & SEAT_CLOSED) != 0;
        }

        public void setSpeaking() {
            flags = (flags & ~SEAT_MUTE_MASK) | SEAT_SPEAKING;
        }

        public boolean isSpeaking() {
            return (flags & SEAT_SPEAKING) != 0;
        }

        public void setAudioMutedByOwner() {
            flags = (flags & ~SEAT_SPEAKING) | SEAT_MUTED_AUDIO_BY_OWNER;
        }

        public void unmuteAudioByOwner() {
            flags = flags & ~SEAT_MUTED_AUDIO_BY_OWNER;
        }

        public boolean isAudioMutedByOwner() {
            return (flags & SEAT_MUTED_AUDIO_BY_OWNER) != 0;
        }

        public void setAudioMutedByMe() {
            flags = (flags & ~SEAT_SPEAKING) | SEAT_MUTED_AUDIO_BY_ME;
        }

        public void unmuteAudioByMe() {
            flags = flags & ~SEAT_MUTED_AUDIO_BY_ME;
        }

        public boolean isAudioMutedByMe() {
            return (flags & SEAT_MUTED_AUDIO_BY_ME) != 0;
        }

        public void setVideoMutedByMe() {
            flags = (flags & ~SEAT_SPEAKING) | SEAT_MUTED_VIDEO_BY_ME;
        }

        public void unmuteVideoByMe() {
            flags = flags & ~SEAT_MUTED_VIDEO_BY_ME;
        }

        public boolean isVideoMutedByMe() {
            return (flags & SEAT_MUTED_VIDEO_BY_ME) != 0;
        }
    }
}
