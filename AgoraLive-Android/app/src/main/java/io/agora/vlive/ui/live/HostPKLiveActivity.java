package io.agora.vlive.ui.live;

import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import androidx.appcompat.widget.AppCompatImageView;

import io.agora.rtc.Constants;
import io.agora.rtc.video.ChannelMediaInfo;
import io.agora.rtc.video.ChannelMediaRelayConfiguration;
import io.agora.rtm.ErrorInfo;
import io.agora.rtm.ResultCallback;
import io.agora.vlive.Config;
import io.agora.vlive.R;
import io.agora.vlive.agora.rtm.model.PKMessage;
import io.agora.vlive.proxy.struts.model.SeatInfo;
import io.agora.vlive.proxy.struts.request.PKRequest;
import io.agora.vlive.proxy.struts.request.Request;
import io.agora.vlive.proxy.struts.request.RoomRequest;
import io.agora.vlive.proxy.struts.response.EnterRoomResponse;
import io.agora.vlive.proxy.struts.response.Response;
import io.agora.vlive.proxy.struts.response.RoomListResponse;
import io.agora.vlive.proxy.struts.response.StartStopPkResponse;
import io.agora.vlive.ui.actionsheets.PkRoomListActionSheet;
import io.agora.vlive.ui.components.CameraTextureView;
import io.agora.vlive.ui.components.LiveBottomButtonLayout;
import io.agora.vlive.ui.components.LiveHostNameLayout;
import io.agora.vlive.ui.components.LiveMessageEditLayout;
import io.agora.vlive.ui.components.PkLayout;
import io.agora.vlive.utils.UserUtil;

public class HostPKLiveActivity extends LiveRoomActivity
        implements View.OnClickListener, PkRoomListActionSheet.OnPkRoomSelectedListener {
    private static final String TAG = HostPKLiveActivity.class.getSimpleName();

    private static final int PK_STATE_STOP = 0;
    private static final int PK_STATE_START = 1;

    private RelativeLayout mLayout;
    private FrameLayout mVideoNormalLayout;
    private LiveHostNameLayout mNamePad;
    private PkRoomListActionSheet mPkRoomListActionSheet;
    private AppCompatImageView mStartPkButton;
    private PkLayout mPkLayout;

    private String pkRoomId;
    private boolean mPkStarted;
    private boolean mBroadcastStarted;

    private int mMessageListHeightInNormalMode;

    private ResultCallback<Void> mMessageResultCallback = new ResultCallback<Void>() {
        @Override
        public void onSuccess(Void aVoid) {

        }

        @Override
        public void onFailure(ErrorInfo errorInfo) {

        }
    };

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
        mMessageListHeightInNormalMode = getResources().
                getDimensionPixelOffset(R.dimen.live_message_list_height);

        setContentView(R.layout.activity_pk_host_in);

        mLayout = findViewById(R.id.live_room_pk_room_layout);
        mVideoNormalLayout = findViewById(R.id.live_pk_video_normal_layout);
        mNamePad = findViewById(R.id.pk_host_in_name_pad);

        participants = findViewById(R.id.pk_host_in_participant);
        participants.setUserLayoutListener(this);

        messageList = findViewById(R.id.message_list);
        bottomButtons = findViewById(R.id.pk_host_in_bottom_layout);
        bottomButtons.setLiveBottomButtonListener(this);
        bottomButtons.setRole(isOwner ? LiveBottomButtonLayout.ROLE_OWNER :
                isHost ? LiveBottomButtonLayout.ROLE_HOST :
                        LiveBottomButtonLayout.ROLE_AUDIENCE);

        findViewById(R.id.live_bottom_btn_close).setOnClickListener(this);
        findViewById(R.id.live_bottom_btn_more).setOnClickListener(this);
        findViewById(R.id.live_bottom_btn_fun1).setOnClickListener(this);
        findViewById(R.id.live_bottom_btn_fun2).setOnClickListener(this);

        mStartPkButton = findViewById(R.id.start_pk_button);
        mStartPkButton.setOnClickListener(this);

        messageEditLayout = findViewById(R.id.message_edit_layout);
        mMessageEditText = messageEditLayout.findViewById(LiveMessageEditLayout.EDIT_TEXT_ID);

        mPkLayout = findViewById(R.id.pk_host_layout);

        // At the initialization phase, the room is considered to
        // be in single-broadcast mode.
        // Whether the room is already in PK mode or not depends
        // on the information returned in the "enter room" response.
        setupUIMode(false, isOwner);
        setupSingleBroadcastBehavior(isOwner, !isOwner, !isOwner);

        // If I am the room owner, I will start single broadcasting
        // right now and do not need to start in "enter room" response
        if (isOwner) mBroadcastStarted = true;
    }

    @Override
    protected void onGlobalLayoutCompleted() {
        View topLayout = findViewById(R.id.pk_host_in_top_participant_layout);
        if (topLayout != null) {
            RelativeLayout.LayoutParams params =
                    (RelativeLayout.LayoutParams) topLayout.getLayoutParams();
            params.topMargin += systemBarHeight;
            topLayout.setLayoutParams(params);
        }
    }

    @Override
    public void onEnterRoomResponse(EnterRoomResponse response) {
        if (response.code == Response.SUCCESS) {
            ownerId = response.data.room.owner.userId;
            ownerRtcUid = response.data.room.owner.uid;

            // Determine if I am the owner of a host here because
            // I may leave the room unexpectedly and come once more.
            String myId = config().getUserProfile().getUserId();
            if (!isOwner && myId.equals(response.data.room.owner.userId)) {
                isOwner = true;
            }

            // Result from server if the channel is in PK mode
            mPkStarted = response.data.room.pk.state == PK_STATE_START;
            if (mPkStarted) pkRoomId = response.data.room.pk.pkRoomId;

            runOnUiThread(() -> {
                mNamePad.setName(response.data.room.owner.userName);
                mNamePad.setIcon(UserUtil.getUserRoundIcon(getResources(),
                        response.data.room.owner.userId));

                if (!mPkStarted) {
                    boolean audioMuted = config().isAudioMuted();
                    boolean videoMuted = config().isVideoMuted();

                    if (isOwner && !mBroadcastStarted) {
                        // I created this room and I left this room unexpectedly
                        // not long ago.
                        // This time I came from room list as an audience at first,
                        // but from the server response, I know that this is my room.
                        // I can start my broadcasting right now if not muted.  `
                        audioMuted = response.data.room.owner.enableAudio !=
                                SeatInfo.User.USER_AUDIO_ENABLE;
                        videoMuted = response.data.room.owner.enableVideo !=
                                SeatInfo.User.USER_VIDEO_ENABLE;
                    }

                    setupSingleBroadcastBehavior(isOwner, audioMuted, videoMuted);
                    mBroadcastStarted = true;
                } else {
                    mBroadcastStarted = false;
                    setupUIMode(true, isOwner);
                    setupPkBehavior(isOwner, response.data.room.pk.countDown,
                            response.data.room.pk.pkRoomOwner.userName, null,
                            response.data.room.pk.pkRoomOwner.uid);
                    updatePkGiftRank(response.data.room.pk.hostRoomRank,
                            response.data.room.pk.pkRoomRank);
                }
            });
        }

        super.onEnterRoomResponse(response);
    }

    private void setupUIMode(boolean isPkMode, boolean isOwner) {
        if (isPkMode) {
            mLayout.setBackgroundResource(R.drawable.dark_background);
            mStartPkButton.setVisibility(View.GONE);
            mVideoNormalLayout.setVisibility(View.GONE);
            mPkLayout.setVisibility(View.VISIBLE);
            mPkLayout.setHost(isOwner);
        } else {
            mLayout.setBackground(null);
            mStartPkButton.setVisibility(isOwner ? View.VISIBLE : View.GONE);
            mPkLayout.setVisibility(View.GONE);
            mVideoNormalLayout.setVisibility(View.VISIBLE);
        }

        setupMessageListLayout(isPkMode);
        bottomButtons.setRole(isOwner ? LiveBottomButtonLayout.ROLE_OWNER
                : LiveBottomButtonLayout.ROLE_AUDIENCE);
        bottomButtons.setBeautyEnabled(config().isBeautyEnabled());
    }

    /**
     * Must be called after the desirable UI mode is already set up
     */
    private void setupPkBehavior(boolean isOwner, long remaining,
                                 String remoteName, PKMessage.RelayConfig config, int remoteUidForAudience) {
        mPkLayout.setHost(isOwner);
        mPkLayout.setOtherHostName(remoteName);
        mPkLayout.startCountDownTimer(remaining);
        if (!isOwner) {
            mPkLayout.setOnClickGotoPeerChannelListener(view -> enterAnotherPkRoom(pkRoomId));
        }

        if (isOwner) {
            startCameraCapture();
            CameraTextureView cameraTextureView = new CameraTextureView(this);
            mPkLayout.getLeftVideoLayout().removeAllViews();
            mPkLayout.getLeftVideoLayout().addView(cameraTextureView);
            SurfaceView remoteSurfaceView = setupRemoteVideo(config.remote.uid);
            mPkLayout.getRightVideoLayout().removeAllViews();
            mPkLayout.getRightVideoLayout().addView(remoteSurfaceView);
            rtcEngine().muteLocalAudioStream(false);
            rtcEngine().muteLocalVideoStream(false);
            startMediaRelay(config);
        } else {
            SurfaceView surfaceView = setupRemoteVideo(ownerRtcUid);
            surfaceView.setZOrderMediaOverlay(true);
            mPkLayout.getLeftVideoLayout().removeAllViews();
            mPkLayout.getLeftVideoLayout().addView(surfaceView);
            SurfaceView remoteSurfaceView = setupRemoteVideo(remoteUidForAudience);
            remoteSurfaceView.setZOrderMediaOverlay(true);
            mPkLayout.getRightVideoLayout().removeAllViews();
            mPkLayout.getRightVideoLayout().addView(remoteSurfaceView);
        }
    }

    /**
     * Must be called after the desirable UI mode is already set up
     */
    private void setupSingleBroadcastBehavior(boolean isOwner, boolean audioMuted, boolean videoMuted) {
        myRtcRole = isOwner ? Constants.CLIENT_ROLE_BROADCASTER
                : Constants.CLIENT_ROLE_AUDIENCE;
        rtcEngine().setClientRole(myRtcRole);

        if (isOwner) {
            startCameraCapture();
            CameraTextureView cameraTextureView = new CameraTextureView(this);
            mVideoNormalLayout.addView(cameraTextureView);
        } else {
            SurfaceView surfaceView = setupRemoteVideo(ownerRtcUid);
            mVideoNormalLayout.removeAllViews();
            mVideoNormalLayout.addView(surfaceView);
        }

        config().setAudioMuted(audioMuted);
        config().setVideoMuted(videoMuted);
        rtcEngine().muteLocalAudioStream(audioMuted);
        rtcEngine().muteLocalVideoStream(videoMuted);
    }

    private void startMediaRelay(PKMessage.RelayConfig config) {
        ChannelMediaRelayConfiguration relayConfig = new ChannelMediaRelayConfiguration();
        relayConfig.setSrcChannelInfo(toChannelMediaInfo(config.local));
        relayConfig.setDestChannelInfo(config.proxy.channelName, toChannelMediaInfo(config.proxy));
        rtcEngine().startChannelMediaRelay(relayConfig);
    }

    private ChannelMediaInfo toChannelMediaInfo(PKMessage.ChannelRelayInfo proxy) {
        return new ChannelMediaInfo(proxy.channelName, proxy.token, proxy.uid);
    }

    private void setupMessageListLayout(boolean isPkMode) {
        RelativeLayout.LayoutParams params =
                (RelativeLayout.LayoutParams) messageList.getLayoutParams();
        if (isPkMode) {
            params.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
            params.addRule(RelativeLayout.BELOW, R.id.pk_host_layout);
        } else {
            params.height = mMessageListHeightInNormalMode;
            params.removeRule(RelativeLayout.BELOW);
        }
        messageList.setLayoutParams(params);
    }

    private void stopPkMode(boolean isOwner) {
        rtcEngine().stopChannelMediaRelay();
        setupUIMode(false, isOwner);
        setupSingleBroadcastBehavior(isOwner,
                config().isAudioMuted(),
                config().isVideoMuted());
    }

    private void enterAnotherPkRoom(String roomId) {
        rtcEngine().leaveChannel();
        leaveRtmChannel(new ResultCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

            }

            @Override
            public void onFailure(ErrorInfo errorInfo) {

            }
        });

        sendRequest(Request.LEAVE_ROOM, new RoomRequest(
                config().getUserProfile().getToken(), this.roomId));

        enterRoom(roomId);
    }

    private void updatePkGiftRank(int mine, int other) {
        if (mPkStarted && mPkLayout.getVisibility() == View.VISIBLE) {
            mPkLayout.setPoints(mine, other);
        }
    }

    @Override
    public void onChannelMediaRelayStateChanged(int state, int code) {
        if (state == Constants.RELAY_STATE_CONNECTING) {
            Log.d(TAG, "channel media relay is connecting");
        } else if (state == Constants.RELAY_STATE_RUNNING) {
            Log.d(TAG, "channel media relay is running");
        } else if (state == Constants.RELAY_STATE_FAILURE) {
            Log.d(TAG, "channel media relay fails");
            handleChannelMediaRelayFailure(code);
        }
    }

    private void handleChannelMediaRelayFailure(int code) {
        switch (code) {
            case Constants.RELAY_ERROR_FAILED_JOIN_SRC:
                break;
            case Constants.RELAY_ERROR_FAILED_JOIN_DEST:
                break;
            case Constants.RELAY_ERROR_FAILED_PACKET_RECEIVED_FROM_SRC:
                break;
            case Constants.RELAY_ERROR_FAILED_PACKET_SENT_TO_DEST:
                break;
            case Constants.RELAY_ERROR_SERVER_CONNECTION_LOST:
                break;
            case Constants.RELAY_ERROR_SRC_TOKEN_EXPIRED:
                break;
            case Constants.RELAY_ERROR_DEST_TOKEN_EXPIRED:
                break;
        }
    }

    @Override
    public void onChannelMediaRelayEvent(int code) {

    }

    @Override
    public void onRoomListResponse(RoomListResponse response) {
        super.onRoomListResponse(response);
        if (mPkRoomListActionSheet != null && mPkRoomListActionSheet.isShown()) {
            runOnUiThread(() -> mPkRoomListActionSheet.appendUsers(response.data));
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.live_bottom_btn_close:
                curDialog = showDialog(R.string.finish_broadcast_title_owner,
                        R.string.finish_broadcast_message_owner, this);
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
            case R.id.start_pk_button:
                if (isOwner) {
                    mPkRoomListActionSheet = (PkRoomListActionSheet)
                            showActionSheetDialog(ACTION_SHEET_PK_ROOM_LIST, true, true, this);
                    mPkRoomListActionSheet.setup(proxy(), config().getUserProfile().getToken());
                    mPkRoomListActionSheet.requestMorePkRoom();
                }
                break;
        }
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

    @Override
    public void onPkRoomListActionSheetRoomSelected(int position, String roomId, int uid) {
        // The owner has selected another host, and send a
        // peer message to get the other host's permission.
        pkRoomId = roomId;
        Config.UserProfile profile = config().getUserProfile();
        // uid is used both in rtc and rtm engine.
        getMessageManager().applyPk(String.valueOf((long) uid), UserUtil.getUserText(
                profile.getUserId(), profile.getUserName()), this.roomId, mMessageResultCallback);
        dismissActionSheetDialog();
    }

    @Override
    public void onRtmPkReceivedFromAnotherHost(String peerId, String nickname, String pkRoomId) {
        // Received a pk request from another host,
        // here show a dialog to make a decision.
        String title = getResources().getString(R.string.live_room_pk_room_receive_pk_request_title);
        String messageFormat = getResources().getString(R.string.live_room_pk_room_receive_pk_request_message);
        String message = String.format(messageFormat, nickname);
        final Config.UserProfile profile = config().getUserProfile();

        runOnUiThread(() -> curDialog = showDialog(title, message,
                R.string.dialog_positive_button_accept, R.string.dialog_negative_button_refuse,
                view -> {
                    getMessageManager().acceptPk(peerId, UserUtil.getUserText(
                            profile.getUserId(), profile.getUserName()), mMessageResultCallback);

                    PKRequest request = new PKRequest(profile.getToken(), this.roomId, pkRoomId);
                    proxy().sendRequest(Request.PK_START_STOP, request);
                    closeDialog();
                },
                view -> {
                    getMessageManager().rejectPk(peerId, UserUtil.getUserText(
                                profile.getUserId(), profile.getUserName()), mMessageResultCallback);
                    closeDialog();
                }));
    }

    @Override
    public void onStartStopPkResponse(StartStopPkResponse response) {
        Log.i(TAG, "onStartStopPkResponse:" + response.data);
    }

    @Override
    public void onRtmPkAcceptedByTargetHost(String peerId, String nickname) {
        runOnUiThread(() -> showShortToast(getResources().getString(R.string.live_room_pk_room_pk_invitation_accepted)));
    }

    @Override
    public void onRtmPkRejectedByTargetHost(String peerId, String nickname) {
        runOnUiThread(() -> showShortToast(getResources().getString(R.string.live_room_pk_room_pk_invitation_rejected)));
    }

    @Override
    public void onRtmPkStateChanged(PKMessage.PKMessageData messageData) {
        runOnUiThread(() -> {
            if (!mPkStarted && messageData.state == PK_STATE_START) {
                mPkStarted = true;
                pkRoomId = messageData.pkRoomId;
                setupUIMode(true, isOwner);
                setupPkBehavior(isOwner, messageData.countDown, messageData.pkRoomOwner.userName,
                        messageData.relayConfig, 0);
                updatePkGiftRank(messageData.hostRoomRank, messageData.pkRoomRank);
            } else if (mPkStarted && messageData.state == PK_STATE_STOP) {
                stopPkMode(isOwner);
                mPkStarted = false;
            } else if (mPkStarted) {
                updatePkGiftRank(messageData.hostRoomRank, messageData.pkRoomRank);
            }
        });
    }

    @Override
    public void onRtcRemoteVideoStateChanged(int uid, int state, int reason, int elapsed) {
        Log.i(TAG, "onRtcRemoteVideoStateChanged uid:" + uid + " state:" + state);
    }
}
