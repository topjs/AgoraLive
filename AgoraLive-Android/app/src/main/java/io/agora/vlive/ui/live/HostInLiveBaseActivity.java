package io.agora.vlive.ui.live;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import io.agora.vlive.R;
import io.agora.vlive.struts.User;
import io.agora.vlive.ui.actionsheets.InviteUserActionSheet;
import io.agora.vlive.ui.components.LiveHostInSeatAdapter;
import io.agora.vlive.ui.components.LiveMessageEditLayout;
import io.agora.vlive.ui.components.LiveRoomMessageList;

public class HostInLiveBaseActivity extends LiveRoomActivity implements View.OnClickListener,
        LiveHostInSeatAdapter.LiveHostInSeatOnClickedListener,
        InviteUserActionSheet.InviteUserActionSheetListener {
    private static final String TAG = HostInLiveBaseActivity.class.getSimpleName();

    private static final int ROOM_NAME_HINT_COLOR = Color.rgb(101, 101, 101);
    private static final int ROOM_NAME_COLOR = Color.rgb(235, 235, 235);

    private RecyclerView mSeatRecyclerView;
    private LiveHostInSeatAdapter mSeatAdapter;

    private InviteUserActionSheet mInviteUserListActionSheet;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host_in);
        hideStatusBar(false);

        isOwner = false;
        isHost = false;
        initUI();
    }

    private void initUI() {
        setRoomNameText();

        participants = findViewById(R.id.host_in_participant);
        participants.setIconResource("fake_icon_2.jpeg");
        participants.setIconResource("fake_icon_3.jpeg");
        participants.setIconResource("fake_icon_2.jpeg");
        participants.setIconResource("fake_icon_3.jpeg");

        messageList = findViewById(R.id.message_list);
        messageList.addMessage(LiveRoomMessageList.MSG_TYPE_CHAT, "康康有毒", "他说会因为那一分钟而永远记住我，那时候我觉得很动听。但现在我看着时钟，我就告诉我自己，我要从这一分钟开始忘掉");
        messageList.addMessage(LiveRoomMessageList.MSG_TYPE_CHAT, "康康有毒", "他说会因为那一分钟而永远记住我，那时候我觉得很动听。但现在我看着时钟，我就告诉我自己，我要从这一分钟开始忘掉");
        messageList.addMessage(LiveRoomMessageList.MSG_TYPE_CHAT, "起司甜甜", "何必在乎其它人");
        messageList.addMessage(LiveRoomMessageList.MSG_TYPE_CHAT, "康康有毒", "他说会因为那一分钟而永远记住我，那时候我觉得很动听。但现在我看着时钟，我就告诉我自己，我要从这一分钟开始忘掉");
        messageList.addMessage(LiveRoomMessageList.MSG_TYPE_CHAT, "起司甜甜", "何必在乎其它人");
        messageList.addMessage(LiveRoomMessageList.MSG_TYPE_CHAT, "起司甜甜", "何必在乎其它人");
        messageList.addMessage(LiveRoomMessageList.MSG_TYPE_CHAT, "起司甜甜", "何必在乎其它人");
        messageList.addMessage(LiveRoomMessageList.MSG_TYPE_CHAT, "起司甜甜", "何必在乎其它人");
        messageList.addMessage(LiveRoomMessageList.MSG_TYPE_CHAT, "起司甜甜", "何必在乎其它人");
        messageList.addMessage(LiveRoomMessageList.MSG_TYPE_CHAT, "起司甜甜", "何必在乎其它人");
        messageList.addMessage(LiveRoomMessageList.MSG_TYPE_CHAT, "起司甜甜", "何必在乎其它人");
        messageList.addMessage(LiveRoomMessageList.MSG_TYPE_CHAT, "起司甜甜", "何必在乎其它人");
        messageList.notifyDataSetChanged();

        mSeatRecyclerView = findViewById(R.id.live_host_in_seat_recycler);
        GridLayoutManager layoutManager = new GridLayoutManager(this,
                3, RecyclerView.VERTICAL, false);
        mSeatRecyclerView.setLayoutManager(layoutManager);
        mSeatAdapter = new LiveHostInSeatAdapter(this);
        mSeatAdapter.setIsHost(isHost);
        mSeatAdapter.setSeatListener(this);
        mSeatRecyclerView.setAdapter(mSeatAdapter);

        bottomButtons = findViewById(R.id.host_in_bottom_layout);
        bottomButtons.setLiveBottomButtonListener(this);
        bottomButtons.setHost(isHost);
        if (isHost) bottomButtons.setBeautyEnabled(application().states().isBeautyEnabled());

        findViewById(R.id.live_bottom_btn_close).setOnClickListener(this);
        findViewById(R.id.live_bottom_btn_more).setOnClickListener(this);
        findViewById(R.id.live_bottom_btn_fun1).setOnClickListener(this);
        findViewById(R.id.live_bottom_btn_fun2).setOnClickListener(this);

        messageEditLayout = findViewById(R.id.message_edit_layout);
        mMessageEditText = messageEditLayout.findViewById(LiveMessageEditLayout.EDIT_TEXT_ID);
    }

    private void setRoomNameText() {
        String nameHint = getResources().getString(R.string.live_host_in_room_name_hint);
        SpannableString name = new SpannableString(nameHint + roomName);
        name.setSpan(new ForegroundColorSpan(ROOM_NAME_HINT_COLOR),
                0, nameHint.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        name.setSpan(new AbsoluteSizeSpan(getResources().getDimensionPixelSize(R.dimen.text_size_medium)),
                0, nameHint.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        name.setSpan(new ForegroundColorSpan(ROOM_NAME_COLOR),
                nameHint.length(), name.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        name.setSpan(new AbsoluteSizeSpan(getResources().getDimensionPixelSize(R.dimen.text_size_normal)),
                nameHint.length(), name.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);

        ((AppCompatTextView) findViewById(R.id.host_in_room_name)).setText(name);
    }

    @Override
    protected void onGlobalLayoutCompleted() {
        View topLayout = findViewById(R.id.host_in_top_participant_layout);
        RelativeLayout.LayoutParams params =
                (RelativeLayout.LayoutParams) topLayout.getLayoutParams();
        params.topMargin += systemBarHeight;
        topLayout.setLayoutParams(params);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.live_bottom_btn_close:
                curDialog = showDialog(R.string.finish_broadcast_title,
                        R.string.finish_broadcast_message, this);
                break;
            case R.id.live_bottom_btn_more:
                showActionSheetDialog(ACTION_SHEET_TOOL, isHost, true, this);
                break;
            case R.id.live_bottom_btn_fun1:
                if (isHost) {
                    showActionSheetDialog(ACTION_SHEET_BG_MUSIC, true, true, this);
                } else {
                    showActionSheetDialog(ACTION_SHEET_GIFT, false, true, this);
                }
                break;
            case R.id.live_bottom_btn_fun2:
                if (isHost) {
                    // this button is hidden when current user is not host.
                    showActionSheetDialog(ACTION_SHEET_BEAUTY, true, true, this);
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        curDialog = showDialog(R.string.finish_broadcast_title,
                R.string.finish_broadcast_message,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        closeDialog();
                        finish();
                    }
                });
    }

    @Override
    public void finish() {
        super.finish();
        bottomButtons.clearStates(application());
    }

    @Override
    public void onHostInviteClicked(int position) {
        Log.i(TAG, "onHostInviteClicked:" + position);
        mInviteUserListActionSheet = (InviteUserActionSheet) showActionSheetDialog(
                ACTION_SHEET_ROOM_USER, isHost, true, this);
        requestAudienceList();
    }

    @Override
    public void onAudienceApplyClicked(int position) {
        Log.i(TAG, "onAudienceApplyClicked:" + position);
        curDialog = showDialog(R.string.live_room_host_in_audience_apply_title,
                R.string.live_room_host_in_audience_apply_message,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        audienceApplyForSeat();
                        closeDialog();
                    }
                });
    }

    private void audienceApplyForSeat() {
        Log.i(TAG, "audience apply for a seat:");
    }

    @Override
    public void onSeatPositionCloseClicked(int position) {
        Log.i(TAG, "onAudienceApplyClicked:" + position);
    }

    private void requestAudienceList() {

    }

    @Override
    public void onAudienceInvited(User user) {
        Log.i(TAG, "onAudienceInvited");
    }
}
