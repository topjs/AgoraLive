package io.agora.vlive.ui.components;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import io.agora.vlive.R;

public class LiveHostInSeatAdapter extends RecyclerView.Adapter {
    public interface LiveHostInSeatOnClickedListener {
        void onHostInviteClicked(int position);
        void onAudienceApplyClicked(int position);
        void onSeatPositionCloseClicked(int position);
    }

    private static final int MAX_SEAT = 6;

    private Context mContext;
    private LayoutInflater mInflater;
    private ArrayList<Seat> mSeatList;
    private boolean mIsHost;

    private LiveHostInSeatOnClickedListener mListener;

    public LiveHostInSeatAdapter(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
        initSeatList();
    }

    private void initSeatList() {
        mSeatList = new ArrayList<>(MAX_SEAT);
        for (int i = 0; i < MAX_SEAT; i++) {
            Seat seat = new Seat();
            seat.id = i + 1;
            seat.state.setOpen();
            mSeatList.add(seat);
        }
    }

    public void setIsHost(boolean isHost) {
        mIsHost = isHost;
    }

    public void setSeatListener(LiveHostInSeatOnClickedListener listener) {
        mListener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SeatViewHolder(mInflater.inflate(
                R.layout.live_host_in_seat_item,
                parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        Seat seat = mSeatList.get(position);
        SeatViewHolder seatViewHolder = (SeatViewHolder) holder;
        seatViewHolder.id.setText(String.valueOf(seat.id));

        if (seat.state.isOpen()) {
            seatViewHolder.icon.setImageResource(R.drawable.live_seat_invite);
            int textRes = mIsHost ?
                    R.string.live_host_in_seat_state_open_host :
                    R.string.live_host_in_seat_state_open_audience;
            seatViewHolder.text.setText(textRes);
        } else if (seat.state.isClosed()) {
            seatViewHolder.text.setText(R.string.live_host_in_seat_state_closed);
            seatViewHolder.icon.setImageResource(R.drawable.live_seat_close);
        } else if (seat.state.isTaken()) {

        }

        if (seat.state.isOpen() || seat.state.isClosed()) {
            final SeatState state = seat.state;
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (state.isOpen() && mListener != null) {
                        if (mIsHost) {
                            mListener.onHostInviteClicked(position);
                        } else {
                            mListener.onAudienceApplyClicked(position);
                        }
                    } else if (state.isClosed() && mListener != null) {
                        mListener.onSeatPositionCloseClicked(position);
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return MAX_SEAT;
    }

    private class SeatViewHolder extends RecyclerView.ViewHolder {
        AppCompatTextView id;
        AppCompatTextView text;
        AppCompatImageView icon;

        SeatViewHolder(@NonNull View itemView) {
            super(itemView);
            id = itemView.findViewById(R.id.host_in_seat_item_id);
            text = itemView.findViewById(R.id.seat_item_operation_text);
            icon = itemView.findViewById(R.id.seat_item_operation_icon);
        }
    }

    public static class Seat {
        int id;
        String user;
        Bitmap icon;
        SeatState state = new SeatState();
    }

    public static class SeatState {
        final int SEAT_TAKEN = 1;
        final int SEAT_OPEN = 1 << 1;
        final int SEAT_CLOSED = 1 << 2;
        final int SEAT_STATUS_MASK = SEAT_TAKEN | SEAT_OPEN | SEAT_CLOSED;

        final int SEAT_SPEAKING = 1 << 3;
        final int SEAT_MUTED = 1 << 4;
        final int SEAT_MUTE_MASK = SEAT_SPEAKING | SEAT_MUTED;

        int flags;

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

        public void setMuted() {
            flags = (flags & ~SEAT_MUTE_MASK) | SEAT_MUTED;
        }

        public boolean isMuted() {
            return (flags & SEAT_MUTED) != 0;
        }
    }
}
