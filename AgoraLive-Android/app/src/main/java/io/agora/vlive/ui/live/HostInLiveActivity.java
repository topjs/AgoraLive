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

public class HostInLiveActivity extends BaseLiveActivity {
    private LiveHostNameLayout mNamePad;
    private LiveRoomParticipantLayout mParticipants;
    private LiveRoomMessageList mMessageList;
    private LiveBottomButtonLayout mBottomButtons;
    private boolean mIsHost;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host_in);
        hideStatusBar(false);
        mIsHost = getIntent().getBooleanExtra(Global.Constants.KEY_IS_HOST, false);
        initUI();
    }

    private void initUI() {
        mNamePad = findViewById(R.id.host_in_name_pad);
        mNamePad.setName("康康有毒");
        mNamePad.setIconResource("fake_icon_1.jpeg");

        mParticipants = findViewById(R.id.host_in_participant);
        mParticipants.setIconResource("fake_icon_2.jpeg");
        mParticipants.setIconResource("fake_icon_3.jpeg");

        mMessageList = findViewById(R.id.message_list);
        mMessageList.addMessage("康康有毒", "他说会因为那一分钟而永远记住我，那时候我觉得很动听。但现在我看着时钟，我就告诉我自己，我要从这一分钟开始忘掉");
        mMessageList.addMessage("起司甜甜", "何必在乎其它人");
        mMessageList.notifyDataSetChanged();

        mBottomButtons = findViewById(R.id.host_in_bottom_layout);
        mBottomButtons.setHost(mIsHost);
    }

    @Override
    protected void onGlobalLayoutCompleted() {
        View topLayout = findViewById(R.id.host_in_top_participant_layout);
        RelativeLayout.LayoutParams params =
                (RelativeLayout.LayoutParams) topLayout.getLayoutParams();
        params.topMargin += systemBarHeight;
        topLayout.setLayoutParams(params);
    }
}
