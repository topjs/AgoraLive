package io.agora.vlive.ui.live;

import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import io.agora.rtc.Constants;
import io.agora.vlive.R;
import io.agora.vlive.proxy.struts.model.SeatInfo;
import io.agora.vlive.proxy.struts.response.EnterRoomResponse;
import io.agora.vlive.proxy.struts.response.Response;
import io.agora.vlive.ui.components.CameraTextureView;
import io.agora.vlive.ui.components.LiveBottomButtonLayout;
import io.agora.vlive.ui.components.LiveHostNameLayout;
import io.agora.vlive.ui.components.LiveMessageEditLayout;
import io.agora.vlive.utils.UserUtil;

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
        super.onPermissionGranted();
    }

    private void initUI() {
        setContentView(R.layout.activity_single_host);

        mNamePad = findViewById(R.id.single_live_name_pad);

        participants = findViewById(R.id.single_live_participant);
        participants.setUserLayoutListener(this);

        bottomButtons = findViewById(R.id.single_live_bottom_layout);
        bottomButtons.setLiveBottomButtonListener(this);
        bottomButtons.setRole(isOwner ? LiveBottomButtonLayout.ROLE_OWNER :
                isHost ? LiveBottomButtonLayout.ROLE_HOST :
                        LiveBottomButtonLayout.ROLE_AUDIENCE);
        if (isOwner || isHost) {
            bottomButtons.setBeautyEnabled(config().isBeautyEnabled());
        }

        mVideoLayout = findViewById(R.id.single_live_video_layout);

        if (isOwner) {
            becomesOwner(false, false);
        }

        findViewById(R.id.live_bottom_btn_close).setOnClickListener(this);
        findViewById(R.id.live_bottom_btn_more).setOnClickListener(this);
        findViewById(R.id.live_bottom_btn_fun1).setOnClickListener(this);
        findViewById(R.id.live_bottom_btn_fun2).setOnClickListener(this);

        messageList = findViewById(R.id.message_list);
        messageEditLayout = findViewById(R.id.message_edit_layout);
        messageEditText = messageEditLayout.findViewById(LiveMessageEditLayout.EDIT_TEXT_ID);

        rtcStatsView = findViewById(R.id.single_host_rtc_stats);
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
                curDialog = showDialog(R.string.finish_broadcast_title_owner,
                        R.string.finish_broadcast_message_owner, this);
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
            ownerId = response.data.room.owner.userId;
            ownerRtcUid = response.data.room.owner.uid;

            // Determine if I am the owner of a host here because
            // I may leave the room unexpectedly and come once more.
            String myId = config().getUserProfile().getUserId();
            if (!isOwner && myId.equals(response.data.room.owner.userId)) {
                isOwner = true;
                myRtcRole = Constants.CLIENT_ROLE_BROADCASTER;
                rtcEngine().setClientRole(myRtcRole);
            }

            runOnUiThread(() -> {
                if (isOwner) {
                    becomesOwner(response.data.room.owner.enableAudio !=
                                    SeatInfo.User.USER_AUDIO_ENABLE,
                            response.data.room.owner.enableVideo !=
                                    SeatInfo.User.USER_VIDEO_ENABLE);
                } else {
                    becomeAudience();
                }

                mNamePad.setName(response.data.room.owner.userName);
                mNamePad.setIcon(UserUtil.getUserRoundIcon(getResources(),
                        response.data.room.owner.userId));
            });
        }
    }

    private void becomesOwner(boolean audioMuted, boolean videoMuted) {
        if (!videoMuted) startCameraCapture();
        bottomButtons.setRole(LiveBottomButtonLayout.ROLE_OWNER);
        bottomButtons.setBeautyEnabled(config().isBeautyEnabled());
        config().setAudioMuted(audioMuted);
        config().setVideoMuted(videoMuted);
        initLocalPreview();
    }

    private void initLocalPreview() {
        CameraTextureView textureView = new CameraTextureView(this);
        mVideoLayout.addView(textureView);
    }

    private void becomeAudience() {
        isHost = false;
        stopCameraCapture();
        bottomButtons.setRole(LiveBottomButtonLayout.ROLE_AUDIENCE);
        rtcEngine().setClientRole(Constants.CLIENT_ROLE_AUDIENCE);
        config().setAudioMuted(true);
        config().setVideoMuted(true);
        setupRemotePreview();
    }

    private void setupRemotePreview() {
        SurfaceView surfaceView = setupRemoteVideo(ownerRtcUid);
        mVideoLayout.addView(surfaceView);
    }

    @Override
    public void onBackPressed() {
        curDialog = showDialog(R.string.finish_broadcast_title_owner,
                R.string.finish_broadcast_message_owner, this);
    }

    @Override
    public void finish() {
        super.finish();
        bottomButtons.clearStates(application());
    }
}
