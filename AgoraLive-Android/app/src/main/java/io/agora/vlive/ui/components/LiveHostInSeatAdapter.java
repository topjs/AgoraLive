package io.agora.vlive.ui.components;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import io.agora.vlive.R;

public class LiveHostInSeatAdapter extends RecyclerView.Adapter {
    private static final int MAX_SEAT = 6;

    private Context mContext;
    private LayoutInflater mInflater;

    public LiveHostInSeatAdapter(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SeatViewHolder(mInflater.inflate(
                R.layout.live_host_in_seat_item,
                parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return MAX_SEAT;
    }

    private class SeatViewHolder extends RecyclerView.ViewHolder {
        public SeatViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

}
