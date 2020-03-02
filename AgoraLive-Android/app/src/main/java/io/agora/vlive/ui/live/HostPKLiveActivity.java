package io.agora.vlive.ui.live;

import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import io.agora.vlive.R;
import io.agora.vlive.ui.components.LiveHostNameLayout;
import io.agora.vlive.ui.components.LiveMessageEditLayout;
import io.agora.vlive.ui.components.LiveRoomMessageList;

public class HostPKLiveActivity extends LiveRoomActivity implements View.OnClickListener {
    private static final String TAG = HostPKLiveActivity.class.getSimpleName();

    private LiveHostNameLayout mNamePad;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pk_host_in);
        hideStatusBar(false);
        initUI();
    }

    private void initUI() {
        mNamePad = findViewById(R.id.pk_host_in_name_pad);
        mNamePad.setName("康康有毒");
        mNamePad.setIconResource("fake_icon_1.jpeg");

        participants = findViewById(R.id.pk_host_in_participant);
        participants.setIconResource("fake_icon_2.jpeg");
        participants.setIconResource("fake_icon_3.jpeg");

        messageList = findViewById(R.id.message_list);
        messageList.addMessage(LiveRoomMessageList.MSG_TYPE_CHAT, "康康有毒", "他说会因为那一分钟而永远记住我，那时候我觉得很动听。但现在我看着时钟，我就告诉我自己，我要从这一分钟开始忘掉");
        messageList.addMessage(LiveRoomMessageList.MSG_TYPE_CHAT, "起司甜甜", "何必在乎其它人");
        messageList.notifyDataSetChanged();

        bottomButtons = findViewById(R.id.pk_host_in_bottom_layout);
        bottomButtons.setLiveBottomButtonListener(this);
        bottomButtons.setHost(isHost);
        if (isHost) bottomButtons.setBeautyEnabled(application().config().isBeautyEnabled());

        findViewById(R.id.live_bottom_btn_close).setOnClickListener(this);
        findViewById(R.id.live_bottom_btn_more).setOnClickListener(this);
        findViewById(R.id.live_bottom_btn_fun1).setOnClickListener(this);
        findViewById(R.id.live_bottom_btn_fun2).setOnClickListener(this);

        messageEditLayout = findViewById(R.id.message_edit_layout);
        mMessageEditText = messageEditLayout.findViewById(LiveMessageEditLayout.EDIT_TEXT_ID);
    }

    @Override
    protected void onGlobalLayoutCompleted() {
        View topLayout = findViewById(R.id.pk_host_in_top_participant_layout);
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
                showActionSheetDialog(ACTION_SHEET_TOOL, isOwner, true, this);
                break;
            case R.id.live_bottom_btn_fun1:
                if (isOwner) {
                    showActionSheetDialog(ACTION_SHEET_BG_MUSIC, true, true, this);
                } else {
                    showActionSheetDialog(ACTION_SHEET_GIFT, false, true, this);
                }
                break;
            case R.id.live_bottom_btn_fun2:
                // this button is hidden when current user is not host.
                if (isOwner) {
                    showActionSheetDialog(ACTION_SHEET_BEAUTY, true, true, this);
                }
                break;
            case R.id.dialog_positive_button:
                closeDialog();
                finish();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        curDialog = showDialog(R.string.finish_broadcast_title,
                R.string.finish_broadcast_message, this);
    }

    @Override
    public void finish() {
        super.finish();
        bottomButtons.clearStates(application());
    }
}
