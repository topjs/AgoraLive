package io.agora.vlive.ui.components;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import io.agora.vlive.R;
import io.agora.vlive.utils.Global;

public class LiveRoomMessageList extends RecyclerView {
    public static final int MSG_TYPE_SYSTEM = 0;
    public static final int MSG_TYPE_CHAT = 1;
    public static final int MSG_TYPE_GIFT = 2;

    private static final int DEFAULT_MESSAGE_TEXT_COLOR = Color.rgb(196, 196, 196);
    private static final int MAX_SAVED_MESSAGE = 50;
    private static final int MESSAGE_ITEM_MARGIN = 16;

    private LiveRoomMessageAdapter mAdapter;
    private LayoutInflater mInflater;

    public LiveRoomMessageList(@NonNull Context context) {
        super(context);
        init();
    }

    public LiveRoomMessageList(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LiveRoomMessageList(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mInflater = LayoutInflater.from(getContext());
        mAdapter = new LiveRoomMessageAdapter();
        LinearLayoutManager layoutManager =
                new LinearLayoutManager(getContext(),
                LinearLayoutManager.VERTICAL, false);
        layoutManager.setStackFromEnd(true);
        setLayoutManager(layoutManager);
        setAdapter(mAdapter);
        addItemDecoration(new MessageItemDecorator());
    }

    public void addMessage(int type, String user, String message, int... index) {
        LiveMessageItem item = new LiveMessageItem(type, user, message);
        if (type == MSG_TYPE_GIFT && index != null) {
            item.giftIndex = index[0];
            item.message = getResources().getString(R.string.live_message_gift_send);
        }
        mAdapter.addMessage(item);
    }

    public void notifyDataSetChanged() {
        mAdapter.notifyDataSetChanged();
    }

    private class LiveRoomMessageAdapter extends Adapter<MessageListViewHolder> {
        private ArrayList<LiveMessageItem> mMessageList = new ArrayList<>();

        @NonNull
        @Override
        public MessageListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == MSG_TYPE_GIFT) {
                return new MessageListViewHolder(mInflater
                        .inflate(R.layout.message_item_gift_layout, parent, false));
            } else {
                return new MessageListViewHolder(mInflater
                        .inflate(R.layout.message_item_layout, parent, false));
            }
        }

        @Override
        public void onBindViewHolder(@NonNull MessageListViewHolder holder, int position) {
            LiveMessageItem item = mMessageList.get(position);
            holder.setMessage(item.user, item.message);

            if (item.type == MSG_TYPE_GIFT && holder.giftIcon != null) {
                holder.giftIcon.setImageResource(Global.Constants.GIFT_ICON_RES[item.giftIndex]);
            }
        }

        @Override
        public int getItemCount() {
            return mMessageList.size();
        }

        @Override
        public int getItemViewType(int position) {
            return mMessageList.get(position).type;
        }

        void addMessage(LiveMessageItem item) {
            if (mMessageList.size() == MAX_SAVED_MESSAGE) {
                mMessageList.remove(mMessageList.size() - 1);
            }
            mMessageList.add(item);
            mAdapter.notifyDataSetChanged();
            scrollToPosition(mMessageList.size() - 1);
        }
    }

    private class MessageListViewHolder extends ViewHolder {
        private AppCompatTextView messageText;
        private AppCompatImageView giftIcon;

        MessageListViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.live_message_item_text);
            giftIcon = itemView.findViewById(R.id.live_message_gift_icon);
        }

        void setMessage(String user, String message) {
            SpannableString messageSpan = new SpannableString(user + ":  " + message);
            messageSpan.setSpan(new StyleSpan(Typeface.BOLD),
                    0, user.length() + 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            messageSpan.setSpan(new ForegroundColorSpan(Color.WHITE),
                    0, user.length() + 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);

            messageSpan.setSpan(new ForegroundColorSpan(DEFAULT_MESSAGE_TEXT_COLOR),
                    user.length() + 2, messageSpan.length(),
                    Spannable.SPAN_INCLUSIVE_INCLUSIVE);

            messageText.setText(messageSpan);
        }
    }

    private class MessageItemDecorator extends ItemDecoration {
        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                                   @NonNull RecyclerView parent, @NonNull State state) {
            super.getItemOffsets(outRect, view, parent, state);
            outRect.top = MESSAGE_ITEM_MARGIN;
            outRect.bottom = MESSAGE_ITEM_MARGIN;

        }
    }

    private static class LiveMessageItem {
        int type;
        String user;
        String message;
        int giftIndex;

        LiveMessageItem(int type, String user, String message) {
            this.type = type;
            this.user = user;
            this.message = message;
        }
    }
}
