package io.agora.vlive.ui.live;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatEditText;

import java.util.ArrayList;
import java.util.List;

import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtm.ErrorInfo;
import io.agora.rtm.ResultCallback;
import io.agora.vlive.Config;
import io.agora.vlive.R;
import io.agora.vlive.agora.rtm.model.GiftRankMessage;
import io.agora.vlive.agora.rtm.model.NotificationMessage;
import io.agora.vlive.proxy.ClientProxy;
import io.agora.vlive.proxy.struts.model.UserProfile;
import io.agora.vlive.proxy.struts.request.CreateRoomRequest;
import io.agora.vlive.proxy.struts.request.Request;
import io.agora.vlive.proxy.struts.request.RoomRequest;
import io.agora.vlive.proxy.struts.request.SendGiftRequest;
import io.agora.vlive.proxy.struts.response.AudienceListResponse;
import io.agora.vlive.proxy.struts.response.CreateRoomResponse;
import io.agora.vlive.proxy.struts.response.EnterRoomResponse;
import io.agora.vlive.proxy.struts.response.Response;
import io.agora.vlive.ui.actionsheets.BackgroundMusicActionSheet;
import io.agora.vlive.ui.actionsheets.BeautySettingActionSheet;
import io.agora.vlive.ui.actionsheets.GiftActionSheet;
import io.agora.vlive.ui.actionsheets.LiveRoomUserListActionSheet;
import io.agora.vlive.ui.actionsheets.LiveRoomSettingActionSheet;
import io.agora.vlive.ui.actionsheets.LiveRoomToolActionSheet;
import io.agora.vlive.ui.actionsheets.VoiceActionSheet;
import io.agora.vlive.ui.components.GiftAnimWindow;
import io.agora.vlive.ui.components.LiveBottomButtonLayout;
import io.agora.vlive.ui.components.LiveMessageEditLayout;
import io.agora.vlive.ui.components.LiveRoomMessageList;
import io.agora.vlive.ui.components.LiveRoomUserLayout;
import io.agora.vlive.ui.components.RtcStatsView;
import io.agora.vlive.utils.GiftUtil;
import io.agora.vlive.utils.Global;

public abstract class LiveRoomActivity extends LiveBaseActivity implements
        BeautySettingActionSheet.BeautyActionSheetListener,
        LiveRoomSettingActionSheet.LiveRoomSettingActionSheetListener,
        BackgroundMusicActionSheet.BackgroundMusicActionSheetListener,
        GiftActionSheet.GiftActionSheetListener,
        LiveRoomToolActionSheet.LiveRoomToolActionSheetListener,
        VoiceActionSheet.VoiceActionSheetListener,
        LiveBottomButtonLayout.LiveBottomButtonListener,
        TextView.OnEditorActionListener,
        LiveRoomUserLayout.UserLayoutListener,
        LiveRoomUserListActionSheet.OnUserSelectedListener {

    private static final String TAG = LiveRoomActivity.class.getSimpleName();
    private static final int IDEAL_MIN_KEYBOARD_HEIGHT = 200;
    private static final int MIN_ONLINE_MUSIC_INTERVAL = 100;

    private Rect mDecorViewRect;
    private int mInputMethodHeight;

    // UI components of a live room
    protected LiveRoomUserLayout participants;
    protected LiveRoomMessageList messageList;
    protected LiveBottomButtonLayout bottomButtons;
    protected LiveMessageEditLayout messageEditLayout;
    protected AppCompatEditText messageEditText;
    protected RtcStatsView rtcStatsView;
    protected Dialog curDialog;

    protected InputMethodManager mInputMethodManager;

    private LiveRoomUserListActionSheet mRoomUserActionSheet;

    // Rtc Engine requires that the calls of startAudioMixing
    // should not be too frequent if online musics are played.
    // The interval is better not to be fewer than 100 ms.
    private volatile long mLastMusicPlayedTimeStamp;

    private boolean mActivityFinished;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().getDecorView().getViewTreeObserver()
                .addOnGlobalLayoutListener(this::detectKeyboardLayout);

        mInputMethodManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    @Override
    protected void onPermissionGranted() {
        if (getIntent().getBooleanExtra(Global.Constants.KEY_CREATE_ROOM, false)) {
            createRoom();
        } else {
            enterRoom(roomId);
        }
    }

    private void detectKeyboardLayout() {
        Rect rect = new Rect();
        getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);

        if (mDecorViewRect == null) {
            mDecorViewRect = rect;
        }

        int diff = mDecorViewRect.height() - rect.height();

        // The global layout listener may be invoked several
        // times when the activity is launched, we need to care
        // about the value of detected input method height to
        // filter out the cases that are not desirable.
        if (diff == mInputMethodHeight) {
            // The input method is still shown
            return;
        }

        if (diff > IDEAL_MIN_KEYBOARD_HEIGHT && mInputMethodHeight == 0) {
            mInputMethodHeight = diff;
            onInputMethodToggle(true, diff);
        } else if (mInputMethodHeight > 0) {
            onInputMethodToggle(false, mInputMethodHeight);
            mInputMethodHeight = 0;
        }
    }

    protected void onInputMethodToggle(boolean shown, int height) {
        RelativeLayout.LayoutParams params =
                (RelativeLayout.LayoutParams) messageEditLayout.getLayoutParams();
        int change = shown ? height : -height;
        params.bottomMargin += change;
        messageEditLayout.setLayoutParams(params);

        if (shown) {
            messageEditText.requestFocus();
            messageEditText.setOnEditorActionListener(this);
        } else {
            messageEditLayout.setVisibility(View.GONE);
        }
    }

    private void createRoom() {
        CreateRoomRequest request = new CreateRoomRequest();
        request.token = config().getUserProfile().getToken();
        request.type = getChannelTypeByTabId();
        request.roomName = roomName;
        sendRequest(Request.CREATE_ROOM, request);
    }

    private int getChannelTypeByTabId() {
        switch (tabId) {
            case Global.Constants.TAB_ID_MULTI:
                return ClientProxy.ROOM_TYPE_HOST_IN;
            case Global.Constants.TAB_ID_PK:
                return ClientProxy.ROOM_TYPE_PK;
            case Global.Constants.TAB_ID_SINGLE:
                return ClientProxy.ROOM_TYPE_SINGLE;
        }
        return -1;
    }

    @Override
    public void onCreateRoomResponse(CreateRoomResponse response) {
        roomId = response.data;
        enterRoom(roomId);
    }

    protected void enterRoom(String roomId) {
        RoomRequest request = new RoomRequest(config().getUserProfile().getToken(), roomId);
        sendRequest(Request.ENTER_ROOM, request);
    }

    @Override
    public void onEnterRoomResponse(EnterRoomResponse response) {
        if (response.code == Response.SUCCESS) {
            Config.UserProfile profile = config().getUserProfile();
            profile.setRtcToken(response.data.user.rtcToken);
            profile.setRtmToken(response.data.user.rtmToken);
            profile.setAgoraUid(response.data.user.uid);

            rtcChannelName = response.data.room.channelName;
            roomId = response.data.room.roomId;
            roomName = response.data.room.roomName;

            joinRtcChannel();
            joinRtmChannel();

            initUserCount(response.data.room.currentUsers,
                    response.data.room.rankUsers);
        }
    }

    private void initUserCount(final int total, final List<EnterRoomResponse.RankInfo> rankUsers) {
        runOnUiThread(() -> participants.reset(total, rankUsers));
    }

    @Override
    public void onActionSheetBeautyEnabled(boolean enabled) {
        Log.i(TAG, "onActionSheetBeautyEnabled:" + enabled);
        bottomButtons.setBeautyEnabled(enabled);
        enablePreProcess(enabled);
    }

    @Override
    public void onActionSheetBrightnessSelected(float brightness) {
        Log.i(TAG, "onActionSheetBrightnessSelected:" + brightness);
    }

    @Override
    public void onActionSheetSmoothSelected(float smooth) {
        Log.i(TAG, "onActionSheetSmoothSelected:" + smooth);
    }

    @Override
    public void onActionSheetColorTemperatureSelected(float temperature) {
        Log.i(TAG, "onActionSheetColorTemperatureSelected:" + temperature);
    }

    @Override
    public void onActionSheetContrastSelected(int type) {
        Log.i(TAG, "onActionSheetContrastSelected:" + type);
    }

    @Override
    public void onActionSheetResolutionSelected(int index) {
        Log.i(TAG, "onActionSheetResolutionSelected:" + index);
        config().setResolutionIndex(index);
        setVideoConfiguration();
    }

    @Override
    public void onActionSheetFrameRateSelected(int index) {
        Log.i(TAG, "onActionSheetFrameRateSelected:" + index);
        config().setFrameRateIndex(index);
        setVideoConfiguration();
    }

    @Override
    public void onActionSheetBitrateSelected(int bitrate) {
        Log.i(TAG, "onActionSheetBitrateSelected:" + bitrate);
        config().setVideoBitrate(bitrate);
        setVideoConfiguration();
    }

    @Override
    public void onActionSheetSettingBackPressed() {
        Log.i(TAG, "onActionSheetSettingBackPressed:");
        dismissActionSheetDialog();
    }

    @Override
    public void onActionSheetMusicSelected(int index, String name, String url) {
        Log.i(TAG, "onActionSheetMusicSelected:" + name);
        long now = System.currentTimeMillis();
        if (now - mLastMusicPlayedTimeStamp > MIN_ONLINE_MUSIC_INTERVAL) {
            rtcEngine().startAudioMixing(url, false, false, -1);
            bottomButtons.setMusicPlaying(true);
            mLastMusicPlayedTimeStamp = now;
        }
    }

    @Override
    public void onActionSheetMusicStopped() {
        Log.i(TAG, "onActionSheetMusicStopped");
        rtcEngine().stopAudioMixing();
        bottomButtons.setMusicPlaying(false);
    }

    @Override
    public void onActionSheetGiftSend(String name, int index, int value) {
        dismissActionSheetDialog();
        SendGiftRequest request = new SendGiftRequest(config().
                getUserProfile().getToken(), roomId, index);
        sendRequest(Request.SEND_GIFT, request);
    }

    @Override
    public void onActionSheetEarMonitoringClicked(boolean monitor) {
        Log.i(TAG, "onActionSheetEarMonitoringClicked:" + monitor);
        rtcEngine().enableInEarMonitoring(monitor);
    }

    @Override
    public void onActionSheetRealDataClicked() {
        Log.i(TAG, "onActionSheetRealDataClicked");
        if (rtcStatsView != null) {
            runOnUiThread(() -> {
                int visibility = rtcStatsView.getVisibility();
                if (visibility == View.VISIBLE) {
                    rtcStatsView.setVisibility(View.GONE);
                } else if (visibility == View.GONE) {
                    rtcStatsView.setVisibility(View.VISIBLE);
                    rtcStatsView.setLocalStats(0, 0, 0, 0);
                }
            });
        }
    }

    @Override
    public void onActionSheetShareClicked() {
        Log.i(TAG, "onActionSheetShareClicked");
    }

    @Override
    public void onActionSheetSettingClicked() {
        Log.i(TAG, "onActionSheetSettingClicked");
        showActionSheetDialog(ACTION_SHEET_VIDEO, isHost, false, this);
    }

    @Override
    public void onActionSheetRotateClicked() {
        Log.i(TAG, "onActionSheetRotateClicked");
        switchCamera();
    }

    @Override
    public void onActionSheetVideoClicked(boolean muted) {
        Log.i(TAG, "onActionSheetVideoClicked:" + muted);
        if (isHost) rtcEngine().muteLocalVideoStream(muted);
    }

    @Override
    public void onActionSheetSpeakerClicked(boolean muted) {
        Log.i(TAG, "onActionSheetSpeakerClicked:" + muted);
        if (isHost) rtcEngine().muteLocalAudioStream(muted);
    }

    @Override
    public void onActionSheetAudioRouteSelected(int type) {
        Log.i(TAG, "onActionSheetAudioRouteSelected:" + type);
    }

    @Override
    public void onActionSheetAudioRouteEnabled(boolean enabled) {
        Log.i(TAG, "onActionSheetAudioRouteEnabled:" + enabled);
    }

    @Override
    public void onActionSheetAudioBackPressed() {
        Log.i(TAG, "onActionSheetAudioBackPressed");
        dismissActionSheetDialog();
    }

    @Override
    public void onLiveBottomLayoutShowMessageEditor() {
        if (messageEditLayout != null) {
            messageEditLayout.setVisibility(View.VISIBLE);
            messageEditText.requestFocus();
            mInputMethodManager.showSoftInput(messageEditText, 0);
        }
    }

    @Override
    public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            Editable editable = messageEditText.getText();
            if (TextUtils.isEmpty(editable)) {
                showShortToast(getResources().getString(R.string.live_send_empty_message));
            } else {
                sendChatMessage(editable.toString());
                messageEditText.setText("");
            }

            mInputMethodManager.hideSoftInputFromWindow(messageEditText.getWindowToken(), 0);
            return true;
        }
        return false;
    }

    private void sendChatMessage(String content) {
        Config.UserProfile profile = config().getUserProfile();
        getMessageManager().sendChatMessage(profile.getUserId(),
                profile.getUserName(), content, new ResultCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

            }

            @Override
            public void onFailure(ErrorInfo errorInfo) {

            }
        });
        messageList.addMessage(LiveRoomMessageList.MSG_TYPE_CHAT, profile.getUserName(), content);
    }

    protected boolean isCurDialogShowing() {
        return curDialog != null && curDialog.isShowing();
    }

    protected void closeDialog() {
        if (isCurDialogShowing()) {
            curDialog.dismiss();
        }
    }

    @Override
    public void onUserLayoutShowUserList(View view) {
        // Show all user info list
        mRoomUserActionSheet = (LiveRoomUserListActionSheet)
                showActionSheetDialog(ACTION_SHEET_ROOM_USER, isHost, true, this);
        mRoomUserActionSheet.setup(proxy(), this, roomId, config().getUserProfile().getToken());
        mRoomUserActionSheet.requestMoreAudience();
    }

    @Override
    public void onAudienceListResponse(AudienceListResponse response) {
        List<UserProfile> userList = new ArrayList<>();
        for (AudienceListResponse.AudienceInfo info : response.data.list) {
            UserProfile profile = new UserProfile();
            profile.setUserId(info.userId);
            profile.setUserName(info.userName);
            profile.setAvatar(info.avatar);
            userList.add(profile);
        }

        if (mRoomUserActionSheet != null && mRoomUserActionSheet.getVisibility() == View.VISIBLE) {
            runOnUiThread(() -> mRoomUserActionSheet.appendUsers(userList));
        }
    }

    @Override
    public void onActionSheetUserListItemSelected(String userId, String userName) {
        // Called when clicking an online user's name, and want to see the detail
        Log.d(TAG, "onActionSheetUserListItemSelected:" + userId);
    }

    @Override
    public void onRtmChannelMessageReceived(String peerId, String nickname, String content) {
        runOnUiThread(() -> messageList.addMessage(LiveRoomMessageList.MSG_TYPE_CHAT, nickname, content));
    }

    @Override
    public void onRtmRoomGiftRankChanged(int total, List<GiftRankMessage.GiftRankItem> list) {
        // The rank of user sending gifts has changed. The client
        // needs to update UI in this callback.
        if (list == null) return;

        List<EnterRoomResponse.RankInfo> rankList = new ArrayList<>();
        for (GiftRankMessage.GiftRankItem item : list) {
            EnterRoomResponse.RankInfo info = new EnterRoomResponse.RankInfo();
            info.userId = item.userId;
            info.userName = item.userName;
            info.avatar = item.avatar;
            rankList.add(info);
        }

        runOnUiThread(() -> participants.reset(rankList));
    }

    @Override
    public void onRtmGiftMessage(String fromUserId, String fromUserName, String toUserId, String toUserName, int giftId) {
        runOnUiThread(() -> {
            String from = TextUtils.isEmpty(fromUserName) ? fromUserId : fromUserName;
            String to = TextUtils.isEmpty(toUserName) ? toUserId : toUserName;
            messageList.addMessage(LiveRoomMessageList.MSG_TYPE_GIFT, from, to, giftId);

            GiftAnimWindow window = new GiftAnimWindow(LiveRoomActivity.this, R.style.gift_anim_window);
            window.setAnimResource(GiftUtil.getGiftAnimRes(giftId));
            window.show();
        });
    }

    @Override
    public void onRtmChannelNotification(int total, List<NotificationMessage.NotificationItem> list) {
        // User enter & leave notifications.
        runOnUiThread(() -> {
            // update room user count
            participants.reset(total);
            for (NotificationMessage.NotificationItem item : list) {
                messageList.addMessage(LiveRoomMessageList.MSG_TYPE_SYSTEM, item.userName, "", item.state);
            }
        });
    }

    @Override
    public void onRtmLeaveMessage() {
        runOnUiThread(this::leaveRoom);
    }

    @Override
    public void onStart() {
        super.onStart();
        if ((isOwner || isHost) && !config().isVideoMuted()) {
            startCameraCapture();
        }
    }

    @Override
    public void onRtcJoinChannelSuccess(String channel, int uid, int elapsed) {
        Log.d(TAG, "onRtcJoinChannelSuccess:" + channel + " uid:" + (uid & 0xFFFFFFFFL));
    }

    @Override
    public void onRtcRemoteVideoStateChanged(int uid, int state, int reason, int elapsed) {
        Log.d(TAG, "onRtcRemoteVideoStateChanged: " + (uid & 0xFFFFFFFFL) +
                " state:" + state + " reason:" + reason);
    }

    @Override
    public void onRtcStats(IRtcEngineEventHandler.RtcStats stats) {
        runOnUiThread(() -> {
            if (rtcStatsView != null && rtcStatsView.getVisibility() == View.VISIBLE) {
                rtcStatsView.setLocalStats(stats.rxKBitRate,
                        stats.rxPacketLossRate, stats.txKBitRate,
                        stats.txPacketLossRate);
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        if ((isHost || isOwner) && !config().isVideoMuted()) {
            // If now the app goes to background, stop the camera
            // capture if the host is displaying his video.
            stopCameraCapture();
        }
    }

    @Override
    public void onBackPressed() {
        showExitDialog();
    }

    private void showExitDialog() {
        int titleRes;
        int messageRes;
        if (isHost) {
            titleRes = R.string.finish_broadcast_title_owner;
            messageRes = R.string.finish_broadcast_message_owner;
        } else {
            titleRes = R.string.finish_broadcast_title_audience;
            messageRes = R.string.finish_broadcast_message_audience;
        }
        curDialog = showDialog(titleRes, messageRes, view -> leaveRoom());
    }

    private void leaveRoom() {
        leaveRoom(roomId);
        finish();
        closeDialog();
        dismissActionSheetDialog();
    }

    protected void leaveRoom(String roomId) {
        sendRequest(Request.LEAVE_ROOM, new RoomRequest(
                config().getUserProfile().getToken(), roomId));
    }

    @Override
    public void finish() {
        super.finish();
        mActivityFinished = true;
    }
}
