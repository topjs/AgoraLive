package io.agora.vlive.ui.actionsheets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import io.agora.vlive.utils.Global;
import io.agora.vlive.R;

public class BackgroundMusicActionSheet extends AbstractActionSheet {
    public interface BackgroundMusicActionSheetListener {
        void onBackgroundMusicSelected(int index, String name, String url);
    }

    private BgMusicAdapter mAdapter;
    private int mPaddingHorizontal;
    private int mDividerHeight;
    private int mSelected = -1;

    private BackgroundMusicActionSheetListener mListener;

    public BackgroundMusicActionSheet(Context context) {
        super(context);
        init();
    }

    public BackgroundMusicActionSheet(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BackgroundMusicActionSheet(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    public void setActionSheetListener(AbsActionSheetListener listener) {
        if (listener instanceof BackgroundMusicActionSheetListener) {
            mListener = (BackgroundMusicActionSheetListener) listener;
        }
    }

    private void init() {
        mPaddingHorizontal = getResources().getDimensionPixelSize(
                R.dimen.live_room_action_sheet_margin);
        mDividerHeight = getResources().getDimensionPixelSize(
                R.dimen.live_room_action_sheet_item_divider_height);
        LayoutInflater.from(getContext()).inflate(
                R.layout.action_background_music, this, true);
        RecyclerView recyclerView = findViewById(R.id.live_room_action_sheet_bg_music_recycler);
        LinearLayoutManager layoutManager = new LinearLayoutManager(
                getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        mAdapter = new BgMusicAdapter();
        recyclerView.setAdapter(mAdapter);
        recyclerView.addItemDecoration(new LineDecorator());
    }

    private class BgMusicAdapter extends RecyclerView.Adapter {
        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new BgMusicViewHolder(LayoutInflater.from(getContext()).
                    inflate(R.layout.action_background_music_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            BgMusicViewHolder bgMusicViewHolder = (BgMusicViewHolder) holder;
            bgMusicViewHolder.setMusicInfo(
                    Global.FakeData.BG_MUSIC[position][0],
                    Global.FakeData.BG_MUSIC[position][1]);
            bgMusicViewHolder.setPosition(position);
            bgMusicViewHolder.setPlaying(mSelected == position);
        }

        @Override
        public int getItemCount() {
            return Global.FakeData.BG_MUSIC.length;
        }
    }

    private class BgMusicViewHolder extends RecyclerView.ViewHolder {
        private AppCompatTextView mTitle;
        private AppCompatTextView mArtist;
        private int mPosition;

        BgMusicViewHolder(@NonNull View itemView) {
            super(itemView);
            mTitle = itemView.findViewById(R.id.live_room_action_sheet_bg_music_title);
            mArtist = itemView.findViewById(R.id.live_room_action_sheet_bg_music_artist);
            itemView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    mSelected = mPosition;
                    mAdapter.notifyDataSetChanged();
                    if (mListener != null) mListener.onBackgroundMusicSelected(mPosition,
                            Global.FakeData.BG_MUSIC[mPosition][0], null);
                }
            });
        }

        void setMusicInfo(String title, String artist) {
            mTitle.setText(title);
            mArtist.setText(artist);
        }

        void setPosition(int position) {
            mPosition = position;
        }

        void setPlaying(boolean isPlaying) {
            itemView.setActivated(isPlaying);
        }
    }

    private class LineDecorator extends RecyclerView.ItemDecoration {
        @Override
        public void onDrawOver(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            Rect rect = new Rect();
            Paint paint = new Paint();
            paint.setColor(Global.Constants.DIVIDER_COLOR);

            int count = parent.getChildCount();
            for (int i = 0; i < count; i++) {
                if (mSelected == i + 1) {
                    continue;
                }

                View child = parent.getChildAt(i);
                child.getDrawingRect(rect);
                int startX = rect.left + mPaddingHorizontal;
                int width = rect.right - rect.left - startX * 2;
                int height = rect.bottom - rect.top;
                int startY = height * (i + 1);
                c.drawRect(new Rect(startX, startY,
                        startX + width, startY + mDividerHeight), paint);
            }
        }
    }
}
