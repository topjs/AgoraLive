package io.agora.vlive.ui.live;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

import java.util.List;

import io.agora.capture.video.camera.CameraVideoChannel;
import io.agora.capture.video.camera.VideoModule;
import io.agora.framework.PreprocessorFaceUnity;
import io.agora.framework.modules.channels.ChannelManager;
import io.agora.rtc.Constants;
import io.agora.rtm.ErrorInfo;
import io.agora.rtm.ResultCallback;
import io.agora.vlive.Config;
import io.agora.vlive.R;
import io.agora.vlive.agora.rtm.model.SeatStateMessage;
import io.agora.vlive.proxy.struts.model.SeatInfo;
import io.agora.vlive.proxy.struts.request.AudienceListRequest;
import io.agora.vlive.proxy.struts.request.ModifySeatStateRequest;
import io.agora.vlive.proxy.struts.request.Request;
import io.agora.vlive.proxy.struts.response.AudienceListResponse;
import io.agora.vlive.proxy.struts.response.EnterRoomResponse;
import io.agora.vlive.proxy.struts.response.Response;
import io.agora.vlive.ui.actionsheets.InviteUserActionSheet;
import io.agora.vlive.ui.actionsheets.LiveRoomToolActionSheet;
import io.agora.vlive.ui.components.CameraTextureView;
import io.agora.vlive.ui.components.LiveBottomButtonLayout;
import io.agora.vlive.ui.components.LiveHostNameLayout;
import io.agora.vlive.ui.components.LiveMessageEditLayout;
import io.agora.vlive.utils.Global;
import io.agora.vlive.utils.UserUtil;

public class VirtualHostLiveActivity extends LiveRoomActivity implements View.OnClickListener,
        InviteUserActionSheet.InviteUserActionSheetListener {
    private static final String TAG = VirtualHostLiveActivity.class.getSimpleName();
    private static final int AUDIENCE_SELECT_IMAGE_REQ_CODE = 1;

    private LiveHostNameLayout mNamePad;
    private FrameLayout mOwnerVideoLayout;
    private FrameLayout mHostVideoLayout;
    private AppCompatTextView mFunBtn;
    private boolean mLayoutCalculated;
    private int mVirtualImageSelected;
    private boolean mConnected;
    private String mHostUid;
    private PreprocessorFaceUnity mPreprocessor;
    private InviteUserActionSheet mInviteUserListActionSheet;

    private CameraVideoChannel mCameraChannel;

    // Universal handling of the results of sending rtm messages
    private ResultCallback<Void> mMessageResultCallback = new ResultCallback<Void>() {
        @Override
        public void onSuccess(Void aVoid) {

        }

        @Override
        public void onFailure(ErrorInfo errorInfo) {
            showLongToast("Message error:" + errorInfo.getErrorDescription());
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideStatusBar(true);
    }

    @Override
    protected void onPermissionGranted() {
        initUI();
        super.onPermissionGranted();
    }

    private void initUI() {
        mCameraChannel = (CameraVideoChannel) VideoModule.instance().
                getVideoChannel(ChannelManager.ChannelID.CAMERA);
        mPreprocessor = (PreprocessorFaceUnity) VideoModule.instance().
            getPreprocessor(ChannelManager.ChannelID.CAMERA);
        mVirtualImageSelected = getIntent().getIntExtra(
                Global.Constants.KEY_VIRTUAL_IMAGE, -1);

        setContentView(R.layout.activity_virtual_host);
        mOwnerVideoLayout = findViewById(R.id.virtual_live_owner_video_layout);
        mHostVideoLayout = findViewById(R.id.virtual_live_host_video_layout);
        mHostVideoLayout.setVisibility(View.GONE);

        mFunBtn = findViewById(R.id.virtual_image_function_btn);

        mNamePad = findViewById(R.id.virtual_live_name_pad);
        mNamePad.init(true);

        participants = findViewById(R.id.virtual_live_participant);
        participants.init(true);
        participants.setUserLayoutListener(this);

        bottomButtons = findViewById(R.id.virtual_live_bottom_layout);
        bottomButtons.init(true, true);
        bottomButtons.setLiveBottomButtonListener(this);
        bottomButtons.setRole(isOwner ? LiveBottomButtonLayout.ROLE_OWNER :
                isHost ? LiveBottomButtonLayout.ROLE_HOST :
                        LiveBottomButtonLayout.ROLE_AUDIENCE);

        findViewById(R.id.live_bottom_btn_close).setOnClickListener(this);
        findViewById(R.id.live_bottom_btn_more).setOnClickListener(this);
        findViewById(R.id.live_bottom_btn_fun1).setOnClickListener(this);
        findViewById(R.id.live_bottom_btn_fun2).setOnClickListener(this);

        messageList = findViewById(R.id.message_list);
        messageList.init(true);
        messageEditLayout = findViewById(R.id.message_edit_layout);
        messageEditText = messageEditLayout.findViewById(LiveMessageEditLayout.EDIT_TEXT_ID);

        rtcStatsView = findViewById(R.id.virtual_host_rtc_stats);
        rtcStatsView.setCloseListener(view -> rtcStatsView.setVisibility(View.GONE));

        // In case that the UI is not relocated because
        // the permission request dialog consumes the chance
        onGlobalLayoutCompleted();
    }

    @Override
    protected void onGlobalLayoutCompleted() {
        View topLayout = findViewById(R.id.virtual_live_top_participant_layout);
        if (topLayout != null && !mLayoutCalculated) {
            RelativeLayout.LayoutParams params =
                    (RelativeLayout.LayoutParams) topLayout.getLayoutParams();
            params.topMargin += systemBarHeight;
            topLayout.setLayoutParams(params);
            mLayoutCalculated = true;
        }

        LinearLayout videoLayout = findViewById(R.id.virtual_live_video_layout);
        if (videoLayout != null) {
            RelativeLayout.LayoutParams params =
                    (RelativeLayout.LayoutParams) videoLayout.getLayoutParams();
            params.topMargin += systemBarHeight;
            params.height = displayWidth * 9 / 8;
            videoLayout.setLayoutParams(params);
            mLayoutCalculated = true;
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
                LiveRoomToolActionSheet toolSheet =
                        (LiveRoomToolActionSheet) showActionSheetDialog(
                        ACTION_SHEET_TOOL, tabIdToLiveType(tabId),
                    isOwner || isHost, true, this);
                toolSheet.setVirtualImage(true);
                toolSheet.setEnableInEarMonitoring(inEarMonitorEnabled);
                break;
            case R.id.live_bottom_btn_fun1:
                if (isOwner) {
                    showActionSheetDialog(ACTION_SHEET_BG_MUSIC,
                            tabIdToLiveType(tabId),
                            true, true, this);
                } else {
                    showActionSheetDialog(ACTION_SHEET_GIFT,
                            tabIdToLiveType(tabId),
                            false, true, this);
                }
                break;
            case R.id.live_bottom_btn_fun2:
                // this button is hidden when
                // current user is not host.
                if (isHost || isOwner) {
                    showActionSheetDialog(ACTION_SHEET_BEAUTY,
                            tabIdToLiveType(tabId),
                            true, true, this);
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
                    // If I am the owner, the server must tell me
                    // which image I selected when creating the room
                    mVirtualImageSelected = virtualImageNameToId(
                            response.data.user.virtualAvatar);

                    becomeOwner(response.data.room.owner.enableAudio !=
                                    SeatInfo.User.USER_AUDIO_ENABLE,
                            response.data.room.owner.enableVideo !=
                                    SeatInfo.User.USER_VIDEO_ENABLE);
                } else {
                    becomeAudience();
                }

                mNamePad.setName(response.data.room.owner.userName);
                mNamePad.setIcon(UserUtil.getUserRoundIcon(getResources(),
                        response.data.room.owner.userId));

                // Check if some one has been the host
                List<SeatInfo> seatListInfo = response.data.room.coVideoSeats;
                int uid = 0;
                if (seatListInfo.size() > 0) {
                    SeatInfo info = seatListInfo.get(0);
                    if (info.seat.state == SeatInfo.TAKEN) {
                        mConnected = true;
                        mHostUid = info.user.userId;
                        uid = info.user.uid;
                    }
                }

                if (mConnected) {
                    toChatDisplay();
                    isHost = myId.equals(mHostUid);
                    if (isHost) {
                        mVirtualImageSelected = virtualImageNameToId(
                                response.data.user.virtualAvatar);
                        becomesHost();
                        mFunBtn.setVisibility(View.GONE);
                    } else {
                        SurfaceView surfaceView = setupRemoteVideo(uid);
                        mHostVideoLayout.addView(surfaceView);
                    }
                }
            });
        }
    }

    private void becomeOwner(boolean audioMuted, boolean videoMuted) {
        if (!videoMuted) startCameraCapture();
        mFunBtn.setVisibility(View.VISIBLE);
        mFunBtn.setText(R.string.live_virtual_image_invite);
        bottomButtons.setRole(LiveBottomButtonLayout.ROLE_OWNER);
        rtcEngine().setClientRole(Constants.CLIENT_ROLE_BROADCASTER);
        config().setAudioMuted(audioMuted);
        config().setVideoMuted(videoMuted);
        mPreprocessor.onAnimojiSelected(mVirtualImageSelected);
        CameraTextureView textureView = new CameraTextureView(this);
        mOwnerVideoLayout.addView(textureView);
    }

    private void becomeAudience() {
        isHost = false;
        stopCameraCapture();
        mFunBtn.setVisibility(View.GONE);
        bottomButtons.setRole(LiveBottomButtonLayout.ROLE_AUDIENCE);
        rtcEngine().setClientRole(Constants.CLIENT_ROLE_AUDIENCE);
        config().setAudioMuted(true);
        config().setVideoMuted(true);
        SurfaceView surfaceView = setupRemoteVideo(ownerRtcUid);
        mOwnerVideoLayout.addView(surfaceView);
        mPreprocessor.onAnimojiSelected(-1);
    }

    private void becomesHost() {
        isHost = true;
        mFunBtn.setVisibility(View.VISIBLE);
        mFunBtn.setText(R.string.live_virtual_image_stop_invite);
        rtcEngine().setClientRole(Constants.CLIENT_ROLE_BROADCASTER);
        config().setAudioMuted(false);
        config().setVideoMuted(false);
        bottomButtons.setRole(LiveBottomButtonLayout.ROLE_HOST);
        mPreprocessor.onAnimojiSelected(mVirtualImageSelected);
        toChatDisplay();
        CameraTextureView textureView = new CameraTextureView(this);
        mHostVideoLayout.addView(textureView);
        mCameraChannel.startCapture();
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
        mPreprocessor.onAnimojiSelected(-1);
    }

    public void onInvite(View view) {
        if (mConnected) {
            curDialog = showDialog(R.string.live_virtual_image_stop_invite,
                    R.string.live_virtual_image_stop_invite_message,
                    R.string.dialog_positive_button,
                    R.string.dialog_negative_button,
                    v -> {
                        Config.UserProfile profile = config().getUserProfile();
                        if (isOwner || isHost) {
                            ModifySeatStateRequest request = new ModifySeatStateRequest(
                                    profile.getToken(), roomId,
                                    profile.getUserId(),
                                    1,   // Only one seat here in virtual image live room
                                    SeatInfo.OPEN);
                            request.setVirtualAvatar(
                                    virtualImageIdToName(mVirtualImageSelected));
                            sendRequest(Request.MODIFY_SEAT_STATE, request);
                        }
                    },
                    v -> curDialog.dismiss());
        } else {
            mInviteUserListActionSheet = (InviteUserActionSheet)
                    showActionSheetDialog(ACTION_SHEET_INVITE_AUDIENCE,
                            tabIdToLiveType(tabId), isHost, true, this);
            requestAudienceList();
        }
    }

    private void requestAudienceList() {
        sendRequest(Request.AUDIENCE_LIST, new AudienceListRequest(
                config().getUserProfile().getToken(),
                roomId, null, AudienceListRequest.TYPE_AUDIENCE));
    }

    @Override
    public void onAudienceListResponse(AudienceListResponse response) {
        super.onAudienceListResponse(response);

        if (mInviteUserListActionSheet != null &&
                mInviteUserListActionSheet.isShown()) {
            runOnUiThread(() -> mInviteUserListActionSheet.append(response.data.list));
        }
    }

    @Override
    public void onActionSheetAudienceInvited(int seatId, String peerId, String userName) {
        // seat id is no-use here because there is only one seat available.
        if (mInviteUserListActionSheet != null && mInviteUserListActionSheet.isShown()) {
            dismissActionSheetDialog();
        }

        getMessageManager().invite(peerId, userName,
                config().getUserProfile().getUserId(), seatId, new ResultCallback<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                    }

                    @Override
                    public void onFailure(ErrorInfo errorInfo) {

                    }
                });
    }

    @Override
    public void onRtmInvitedByOwner(String peerId, String nickname, int index) {
        Log.i(TAG, "Invited by room owner " + nickname + " for seat " + index);
        if (isOwner) return;
        String title = getResources().getString(R.string.live_room_host_in_invite_user_list_action_sheet_title);
        String message = getResources().getString(R.string.live_room_virtual_image_invited_message);
        message = String.format(message, nickname);
        final Config.UserProfile profile = config().getUserProfile();
        curDialog = showDialog(title, message,
                R.string.dialog_positive_button_accept, R.string.dialog_negative_button_refuse,
                view -> {
                    // If the audience accepts the invitation,
                    // he should first choose a virtual image.
                    Intent intent = new Intent(this,
                            VirtualImageSelectActivity.class);
                    intent.putExtra(Global.Constants.KEY_PEER_ID, peerId);
                    intent.putExtra(Global.Constants.KEY_NICKNAME, nickname);
                    intent.putExtra(Global.Constants.KEY_AUDIENCE_VIRTUAL_IMAGE, true);
                    startActivityForResult(intent, AUDIENCE_SELECT_IMAGE_REQ_CODE);
                    curDialog.dismiss();
                },
                view -> {
                    getMessageManager().rejectInvitation(peerId, nickname,
                            profile.getUserId(), mMessageResultCallback);
                    curDialog.dismiss();
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) return;

        Config.UserProfile profile = config().getUserProfile();
        String peerId = data.getStringExtra(Global.Constants.KEY_PEER_ID);
        String nickname = data.getStringExtra(Global.Constants.KEY_NICKNAME);
        getMessageManager().acceptInvitation(peerId, nickname,
                profile.getUserId(), 1, mMessageResultCallback);
        mVirtualImageSelected = data.getIntExtra(
                Global.Constants.KEY_VIRTUAL_IMAGE, -1);
        ModifySeatStateRequest request = new ModifySeatStateRequest(
                profile.getToken(), roomId,
                profile.getUserId(),
                1,   // Only one seat here in virtual image live room
                SeatInfo.TAKEN);
        request.setVirtualAvatar(virtualImageIdToName(mVirtualImageSelected));
        sendRequest(Request.MODIFY_SEAT_STATE, request);
        mFunBtn.setVisibility(View.VISIBLE);
        mFunBtn.setText(R.string.live_virtual_image_stop_invite);
    }

    @Override
    public void onRtmInvitationAccepted(String peerId, String nickname, int index) {
        Log.i(TAG, "The audience has accepted your invitation");
        showShortToast(getResources().getString(R.string.invite_success));
    }

    @Override
    public void onRtmInvitationRejected(String peerId, String nickname) {
        Log.i(TAG, "The audience has rejected your invitation");
        String title = getResources().getString(R.string.live_room_host_in_invite_rejected);
        String message = getResources().getString(R.string.live_room_host_in_invite_rejected_message);
        message = String.format(message, nickname);
        curDialog = showSingleButtonConfirmDialog(title, message, view -> curDialog.dismiss());
    }

    @Override
    public void onRtmSeatStateChanged(List<SeatStateMessage.SeatStateMessageDataItem> list) {
        SeatStateMessage.SeatStateMessageDataItem item = list.get(0);
        boolean taken = item.seat.state == SeatInfo.TAKEN;
        mHostUid = item.user.userId;
        String myUid = config().getUserProfile().getUserId();

        runOnUiThread(() -> {
            if (!mConnected && taken && !TextUtils.isEmpty(mHostUid)) {
                mConnected = true;
                if (myUid.equals(item.user.userId)) {
                    becomesHost();
                } else {
                    if (isOwner) {
                        mFunBtn.setVisibility(View.VISIBLE);
                        mFunBtn.setText(R.string.live_virtual_image_stop_invite);
                    }

                    toChatDisplay();
                    mHostVideoLayout.addView(setupRemoteVideo(item.user.uid));
                }
            } else if (mConnected && !taken) {
                toSingleHostDisplay();

                if (isOwner) {
                    mFunBtn.setVisibility(View.VISIBLE);
                    mFunBtn.setText(R.string.live_virtual_image_invite);
                } else {
                    isHost = false;
                    mFunBtn.setVisibility(View.GONE);
                    bottomButtons.setRole(LiveBottomButtonLayout.ROLE_AUDIENCE);
                    rtcEngine().setClientRole(Constants.CLIENT_ROLE_AUDIENCE);
                    mPreprocessor.onAnimojiSelected(-1);
                    mCameraChannel.stopCapture();
                }
                mConnected = false;
            }
        });
    }

    private void toSingleHostDisplay() {
        mHostVideoLayout.removeAllViews();
        mHostVideoLayout.setVisibility(View.GONE);
        LinearLayout.LayoutParams params =
                (LinearLayout.LayoutParams) mHostVideoLayout.getLayoutParams();
        params.width = 0;
        params.weight = 0;
        mHostVideoLayout.setLayoutParams(params);

        params = (LinearLayout.LayoutParams)
                mOwnerVideoLayout.getLayoutParams();
        params.width = LinearLayout.LayoutParams.MATCH_PARENT;
        params.weight = 1;
        mOwnerVideoLayout.setLayoutParams(params);
    }

    private void toChatDisplay() {
        mHostVideoLayout.setVisibility(View.VISIBLE);
        LinearLayout.LayoutParams params =
                (LinearLayout.LayoutParams) mHostVideoLayout.getLayoutParams();
        params.width = 0;
        params.weight = 1;
        mHostVideoLayout.setLayoutParams(params);

        params = (LinearLayout.LayoutParams)
                mOwnerVideoLayout.getLayoutParams();
        params.width = 0;
        params.weight = 1;
        mOwnerVideoLayout.setLayoutParams(params);
    }
}
