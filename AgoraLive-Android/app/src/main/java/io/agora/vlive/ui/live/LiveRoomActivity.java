package io.agora.vlive.ui.live;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
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

import io.agora.vlive.Config;
import io.agora.vlive.R;
import io.agora.vlive.proxy.ClientProxy;
import io.agora.vlive.proxy.model.UserProfile;
import io.agora.vlive.proxy.struts.request.CreateRoomRequest;
import io.agora.vlive.proxy.struts.request.Request;
import io.agora.vlive.proxy.struts.request.RoomRequest;
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
import io.agora.vlive.ui.components.LiveBottomButtonLayout;
import io.agora.vlive.ui.components.LiveMessageEditLayout;
import io.agora.vlive.ui.components.LiveRoomMessageList;
import io.agora.vlive.ui.components.LiveRoomUserLayout;
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
        LiveRoomUserLayout.UserLayoutListener {

    private static final String TAG = LiveRoomActivity.class.getSimpleName();
    private static final int IDEAL_MIN_KEYBOARD_HEIGHT = 200;
    private static final int USER_COUNT_REFRESH_INTERVAL = 5000;
    private static final int MIN_ONLINE_MUSIC_INTERVAL = 100;

    private Rect mDecorViewRect;
    private int mInputMethodHeight;

    // UI components of a live room
    protected LiveRoomUserLayout participants;
    protected LiveRoomMessageList messageList;
    protected LiveBottomButtonLayout bottomButtons;
    protected LiveMessageEditLayout messageEditLayout;
    protected AppCompatEditText mMessageEditText;
    protected Dialog curDialog;

    protected InputMethodManager mInputMethodManager;

    private LiveRoomUserListActionSheet mRoomUserActionSheet;

    private Handler mHandler;
    private UserCountRunnable mUserCountRunnable = new UserCountRunnable();

    // Rtc Engine requires that the calls of startAudioMixing should
    // not be too frequent if online musics are played.
    // The interval is better not to be fewer than 100 ms.
    private volatile long mLastMusicPlayedTimeStamp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().getDecorView().getViewTreeObserver()
                .addOnGlobalLayoutListener(this::detectKeyboardLayout);

        mInputMethodManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);

        mHandler = new Handler(getMainLooper());
        mHandler.postDelayed(mUserCountRunnable, USER_COUNT_REFRESH_INTERVAL);

        if (getIntent().getBooleanExtra(Global.Constants.KEY_CREATE_ROOM, false)) {
            createRoom();
        } else {
            enterRoom();
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
            mMessageEditText.requestFocus();
            mMessageEditText.setOnEditorActionListener(this);
        } else {
            messageEditLayout.setVisibility(View.GONE);
        }
    }

    private void createRoom() {
        CreateRoomRequest request = new CreateRoomRequest();
        request.token = config().getUserProfile().getToken();
        request.type = getChannelTypeByTabId();
        request.roomName = roomName;
        proxy().sendReq(Request.CREATE_ROOM, request);
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
        enterRoom();
    }

    private void enterRoom() {
        RoomRequest request = new RoomRequest();
        request.roomId = roomId;
        request.token = config().getUserProfile().getToken();
        proxy().sendReq(Request.ENTER_ROOM, request);
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
        }
    }

    private class UserCountRunnable implements Runnable {
        @Override
        public void run() {
            requestUserCount();
            mHandler.postDelayed(mUserCountRunnable, USER_COUNT_REFRESH_INTERVAL);
        }
    }

    private void requestUserCount() {
        //TODO
    }

    @Override
    public void onActionSheetBeautyEnabled(boolean enabled) {
        Log.i(TAG, "onActionSheetBeautyEnabled:" + enabled);
        bottomButtons.setBeautyEnabled(enabled);
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
    }

    @Override
    public void onActionSheetFrameRateSelected(int index) {
        Log.i(TAG, "onActionSheetFrameRateSelected:" + index);
    }

    @Override
    public void onActionSheetBitrateSelected(int bitrate) {
        Log.i(TAG, "onActionSheetBitrateSelected:" + bitrate);
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
        Log.i(TAG, "onActionSheetGiftSend:" + name);
        dismissActionSheetDialog();
        messageList.addMessage(LiveRoomMessageList.MSG_TYPE_GIFT, "me", null, index);
    }

    @Override
    public void onActionSheetVoiceClicked() {
        Log.i(TAG, "onActionSheetVoiceClicked");
        showActionSheetDialog(ACTION_SHEET_VOICE, isHost, false, this);
    }

    @Override
    public void onActionSheetRealDataClicked() {
        Log.i(TAG, "onActionSheetRealDataClicked");
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
    }

    @Override
    public void onActionSheetVideoClicked(boolean muted) {
        Log.i(TAG, "onActionSheetVideoClicked:" + muted);
        // call rtc engine to mute/unmute my video, then
        // send a channel message to notify other users in
        // this channel to update their UI
    }

    @Override
    public void onActionSheetSpeakerClicked(boolean muted) {
        Log.i(TAG, "onActionSheetSpeakerClicked:" + muted);
        // call rtc engine to mute/unmute my audio, then
        // send a channel message to notify other users in
        // this channel to update their UI
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
            mMessageEditText.requestFocus();
            mInputMethodManager.showSoftInput(mMessageEditText, 0);
        }
    }

    @Override
    public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            Editable editable = mMessageEditText.getText();
            if (TextUtils.isEmpty(editable)) {
                showShortToast(getResources().getString(R.string.live_send_empty_message));
            } else {
                String message = editable.toString();
                messageList.addMessage(LiveRoomMessageList.MSG_TYPE_CHAT, "me", message);
                mMessageEditText.setText("");
            }

            mInputMethodManager.hideSoftInputFromWindow(mMessageEditText.getWindowToken(), 0);
            return true;
        }
        return false;
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
        mRoomUserActionSheet = (LiveRoomUserListActionSheet)
                showActionSheetDialog(ACTION_SHEET_ROOM_USER, isHost, true, this);
        mRoomUserActionSheet.setRoomInfo(proxy(), this, roomId, config().getUserProfile().getToken());
        mRoomUserActionSheet.requestMoreAudience();
    }

    @Override
    public void onAudienceListResponse(AudienceListResponse response) {
        List<UserProfile> userList = new ArrayList<>();
        for (AudienceListResponse.AudienceInfo info : response.data.list) {
            UserProfile profile = new UserProfile();
            profile.setUserId(info.userId);
            profile.setUserName(info.userName);
            profile.setAvatar(info.avator);
        }

        if (mRoomUserActionSheet != null && mRoomUserActionSheet.getVisibility() == View.VISIBLE) {
            mRoomUserActionSheet.appendUsers(userList);
        }
    }
}
