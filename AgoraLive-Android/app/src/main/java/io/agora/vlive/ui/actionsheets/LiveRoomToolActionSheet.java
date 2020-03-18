package io.agora.vlive.ui.actionsheets;

import android.content.Context;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import io.agora.vlive.R;

public class LiveRoomToolActionSheet extends AbstractActionSheet {
    public interface LiveRoomToolActionSheetListener extends AbsActionSheetListener {
        void onActionSheetVoiceClicked();
        void onActionSheetRealDataClicked();
        void onActionSheetShareClicked();
        void onActionSheetSettingClicked();
        void onActionSheetRotateClicked();
        void onActionSheetVideoClicked(boolean muted);
        void onActionSheetSpeakerClicked(boolean muted);
    }

    private static final int GRID_SPAN = 4;
    private static final int FUNC_COUNT_GUEST = 3;

    private static final int VOICE_INDEX = 0;
    private static final int DATA_INDEX = 1;
    private static final int SHARE_INDEX = 2;
    private static final int SETTING_INDEX = 3;
    private static final int ROTATE_INDEX = 4;
    private static final int VIDEO_INDEX = 5;
    private static final int SPEAKER_INDEX = 6;

    private static final int ICON_RES[] = {
            R.drawable.icon_voice,
            R.drawable.icon_data,
            R.drawable.icon_share,
            R.drawable.icon_setting,
            R.drawable.icon_rotate,
            R.drawable.action_sheet_tool_video,
            R.drawable.action_sheet_tool_speaker
    };

    private RecyclerView mRecycler;
    private String[] mToolNames;
    private boolean mIsHost;
    private int mItemPadding;
    private boolean mMuteVideo;
    private boolean mMuteVoice;
    private LiveRoomToolActionSheetListener mListener;

    public LiveRoomToolActionSheet(Context context) {
        super(context);
        init();
    }

    private void init() {
        mToolNames = getResources().getStringArray(R.array.live_room_action_sheet_tool_names);
        mItemPadding = getResources().getDimensionPixelSize(R.dimen.activity_vertical_margin);

        LayoutInflater.from(getContext()).inflate(R.layout.action_tool, this, true);
        mRecycler = findViewById(R.id.live_room_action_sheet_tool_recycler);
        mRecycler.setLayoutManager(new GridLayoutManager(getContext(), GRID_SPAN));
        mRecycler.setAdapter(new ToolAdapter());
        mRecycler.addItemDecoration(new PaddingDecoration());
    }

    public void setHost(boolean isHost) {
        mIsHost = isHost;
        if (mRecycler != null) {
            mRecycler.getAdapter().notifyDataSetChanged();
        }
    }

    private class ToolAdapter extends RecyclerView.Adapter {
        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ToolViewHolder(LayoutInflater.from(
                    getContext()).inflate(R.layout.action_tool_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ToolViewHolder toolViewHolder = (ToolViewHolder) holder;
            toolViewHolder.setPosition(position);
            toolViewHolder.name.setText(mToolNames[position]);
            toolViewHolder.icon.setImageResource(ICON_RES[position]);

            if (position == VIDEO_INDEX) {
                holder.itemView.setActivated(!mMuteVideo);
            } else if (position == SPEAKER_INDEX) {
                holder.itemView.setActivated(!mMuteVoice);
            }
        }

        @Override
        public int getItemCount() {
            return mIsHost ? ICON_RES.length : FUNC_COUNT_GUEST;
        }
    }

    private class ToolViewHolder extends RecyclerView.ViewHolder {
        AppCompatImageView icon;
        AppCompatTextView name;
        int position;
        ToolViewHolder(@NonNull final View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.live_room_action_sheet_tool_item_name);
            icon = itemView.findViewById(R.id.live_room_action_sheet_tool_item_icon);
            icon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (position == VIDEO_INDEX) {
                        mMuteVideo = !mMuteVideo;
                        itemView.setActivated(!mMuteVideo);
                    } else if (position == SPEAKER_INDEX) {
                        mMuteVoice = !mMuteVoice;
                        itemView.setActivated(!mMuteVoice);
                    }
                    mRecycler.getAdapter().notifyDataSetChanged();
                    handleItemClicked(position);
                }
            });
        }

        void setPosition(int position) {
            this.position = position;
        }
    }

    private void handleItemClicked(int position) {
        if (mListener == null) return;

        switch (position) {
            case VOICE_INDEX:
                mListener.onActionSheetVoiceClicked();
                break;
            case DATA_INDEX:
                mListener.onActionSheetRealDataClicked();
                break;
            case SHARE_INDEX:
                mListener.onActionSheetShareClicked();
                break;
            case SETTING_INDEX:
                mListener.onActionSheetSettingClicked();
                break;
            case ROTATE_INDEX:
                mListener.onActionSheetRotateClicked();
                break;
            case VIDEO_INDEX:
                mListener.onActionSheetVideoClicked(mMuteVideo);
                break;
            case SPEAKER_INDEX:
                mListener.onActionSheetSpeakerClicked(mMuteVoice);
                break;
        }
    }

    private class PaddingDecoration extends RecyclerView.ItemDecoration {
        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            outRect.top = mItemPadding;
            outRect.bottom = mItemPadding;
        }
    }

    @Override
    public void setActionSheetListener(AbsActionSheetListener listener) {
        if (listener instanceof LiveRoomToolActionSheetListener) {
            mListener = (LiveRoomToolActionSheetListener) listener;
        }
    }
}
