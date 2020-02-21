package io.agora.vlive.ui.live;

import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import io.agora.vlive.Global;
import io.agora.vlive.R;
import io.agora.vlive.ui.components.LiveBottomButtonLayout;
import io.agora.vlive.ui.components.LiveHostNameLayout;
import io.agora.vlive.ui.components.LiveRoomMessageList;
import io.agora.vlive.ui.components.LiveRoomParticipantLayout;

public class HostPKLiveActivity extends BaseLiveActivity implements  View.OnClickListener {
    private LiveHostNameLayout mNamePad;
    private LiveRoomParticipantLayout mParticipants;
    private LiveRoomMessageList mMessageList;
    private LiveBottomButtonLayout mBottomButtons;
    private boolean mIsHost;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pk_host_in);
        hideStatusBar(false);
        mIsHost = getIntent().getBooleanExtra(Global.Constants.KEY_IS_HOST, false);
        initUI();
    }

    private void initUI() {
        mNamePad = findViewById(R.id.pk_host_in_name_pad);
        mNamePad.setName("康康有毒");
        mNamePad.setIconResource("fake_icon_1.jpeg");

        mParticipants = findViewById(R.id.pk_host_in_participant);
        mParticipants.setIconResource("fake_icon_2.jpeg");
        mParticipants.setIconResource("fake_icon_3.jpeg");

        mMessageList = findViewById(R.id.message_list);
        mMessageList.addMessage("康康有毒", "他说会因为那一分钟而永远记住我，那时候我觉得很动听。但现在我看着时钟，我就告诉我自己，我要从这一分钟开始忘掉");
        mMessageList.addMessage("起司甜甜", "何必在乎其它人");
        mMessageList.notifyDataSetChanged();

        mBottomButtons = findViewById(R.id.pk_host_in_bottom_layout);
        mBottomButtons.setHost(mIsHost);

        findViewById(R.id.live_bottom_btn_close).setOnClickListener(this);
        findViewById(R.id.live_bottom_btn_more).setOnClickListener(this);
        findViewById(R.id.live_bottom_btn_fun1).setOnClickListener(this);
        findViewById(R.id.live_bottom_btn_fun2).setOnClickListener(this);
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
                showDialog(R.string.finish_broadcast_title,
                        R.string.finish_broadcast_message, this);
                break;
            case R.id.live_bottom_btn_more:
                break;
            case R.id.live_bottom_btn_fun1:
                if (mIsHost) {

                } else {

                }
                break;
            case R.id.live_bottom_btn_fun2:
                // this button is hidden when
                // current user is not host.
                if (mIsHost) {
                    showActionSheetDialog(ACTION_SHEET_BEAUTY);
                }
                break;
            case R.id.dialog_positive_button:
                finish();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        showDialog(R.string.finish_broadcast_title,
                R.string.finish_broadcast_message, this);
    }
}
