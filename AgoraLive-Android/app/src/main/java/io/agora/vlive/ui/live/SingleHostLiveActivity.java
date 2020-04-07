package io.agora.vlive.ui.live;

import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import io.agora.framework.VideoModule;
import io.agora.vlive.R;
import io.agora.vlive.proxy.struts.response.EnterRoomResponse;
import io.agora.vlive.proxy.struts.response.Response;
import io.agora.vlive.ui.components.CameraTextureView;
import io.agora.vlive.ui.components.LiveHostNameLayout;
import io.agora.vlive.ui.components.LiveMessageEditLayout;
import io.agora.vlive.ui.components.LiveRoomMessageList;

public class SingleHostLiveActivity extends LiveRoomActivity implements View.OnClickListener {
    private static final String TAG = SingleHostLiveActivity.class.getSimpleName();

    private LiveHostNameLayout mNamePad;
    private FrameLayout mVideoLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideStatusBar(false);
    }

    @Override
    protected void onPermissionGranted() {
        initUI();
    }

    private void initUI() {
        setContentView(R.layout.activity_single_host);

        mNamePad = findViewById(R.id.single_live_name_pad);

        participants = findViewById(R.id.single_live_participant);
        participants.setUserLayoutListener(this);

        bottomButtons = findViewById(R.id.single_live_bottom_layout);
        bottomButtons.setLiveBottomButtonListener(this);
        bottomButtons.setHost(isOwner);

        mVideoLayout = findViewById(R.id.single_live_video_layout);

        if (isOwner) {
            bottomButtons.setBeautyEnabled(config().isBeautyEnabled());
            startCameraCapture();
            initLocalPreview();
        }

        findViewById(R.id.live_bottom_btn_close).setOnClickListener(this);
        findViewById(R.id.live_bottom_btn_more).setOnClickListener(this);
        findViewById(R.id.live_bottom_btn_fun1).setOnClickListener(this);
        findViewById(R.id.live_bottom_btn_fun2).setOnClickListener(this);

        messageList = findViewById(R.id.message_list);
        messageEditLayout = findViewById(R.id.message_edit_layout);
        mMessageEditText = messageEditLayout.findViewById(LiveMessageEditLayout.EDIT_TEXT_ID);
    }

    private void initLocalPreview() {
        CameraTextureView textureView = new CameraTextureView(this);
        mVideoLayout.addView(textureView);
    }

    @Override
    protected void onGlobalLayoutCompleted() {
        View topLayout = findViewById(R.id.single_live_top_participant_layout);
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
                // this button is hidden when
                // current user is not host.
                if (isHost) {
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
    public void onEnterRoomResponse(EnterRoomResponse response) {
        super.onEnterRoomResponse(response);
        if (response.code == Response.SUCCESS) {
            runOnUiThread(() -> {
                mNamePad.setName(response.data.room.owner.userName);
            });
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
