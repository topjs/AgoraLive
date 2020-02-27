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
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import io.agora.vlive.R;

public class LiveRoomMessageList extends RecyclerView {
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
        setLayoutManager(new LinearLayoutManager(getContext(),
                LinearLayoutManager.VERTICAL, false));
        setAdapter(mAdapter);
        addItemDecoration(new MessageItemDecorator());
    }

    public void addMessage(String user, String message) {
        mAdapter.addMessage(user, message);
    }

    public void notifyDataSetChanged() {
        mAdapter.notifyDataSetChanged();
    }

    private class LiveRoomMessageAdapter extends Adapter<MessageListViewHolder> {
        private ArrayList<LiveMessageItem> mMessageList = new ArrayList<>();

        @NonNull
        @Override
        public MessageListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new MessageListViewHolder(mInflater
                    .inflate(R.layout.message_item_layout, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull MessageListViewHolder holder, int position) {
            LiveMessageItem item = mMessageList.get(position);
            holder.setMessage(item.user, item.message);
        }

        @Override
        public int getItemCount() {
            return mMessageList.size();
        }

        void addMessage(String user, String message) {
            if (mMessageList.size() == MAX_SAVED_MESSAGE) {
                mMessageList.remove(mMessageList.size() - 1);
            }
            mMessageList.add(0, new LiveMessageItem(user, message));
            mAdapter.notifyDataSetChanged();
            scrollToPosition(0);
        }
    }

    private class MessageListViewHolder extends ViewHolder {
        private AppCompatTextView mMessageText;

        MessageListViewHolder(@NonNull View itemView) {
            super(itemView);
            mMessageText = itemView.findViewById(R.id.live_message_item_text);
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

            mMessageText.setText(messageSpan);
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

    public static class LiveMessageItem {
        String user;
        String message;

        LiveMessageItem(String user, String message) {
            this.user = user;
            this.message = message;
        }
    }
}
