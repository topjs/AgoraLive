package io.agora.vlive.ui.live;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatEditText;

import io.agora.vlive.R;
import io.agora.vlive.ui.actionsheets.BackgroundMusicActionSheet;
import io.agora.vlive.ui.actionsheets.BeautySettingActionSheet;
import io.agora.vlive.ui.actionsheets.GiftActionSheet;
import io.agora.vlive.ui.actionsheets.LiveRoomSettingActionSheet;
import io.agora.vlive.ui.actionsheets.LiveRoomToolActionSheet;
import io.agora.vlive.ui.actionsheets.VoiceActionSheet;
import io.agora.vlive.ui.components.LiveBottomButtonLayout;
import io.agora.vlive.ui.components.LiveMessageEditLayout;
import io.agora.vlive.ui.components.LiveRoomMessageList;
import io.agora.vlive.ui.components.LiveRoomParticipantLayout;
import io.agora.vlive.utils.Global;

public abstract class LiveRoomActivity extends BaseLiveActivity implements
        BeautySettingActionSheet.BeautyActionSheetListener,
        LiveRoomSettingActionSheet.LiveRoomSettingActionSheetListener,
        BackgroundMusicActionSheet.BackgroundMusicActionSheetListener,
        GiftActionSheet.GiftActionSheetListener,
        LiveRoomToolActionSheet.LiveRoomToolActionSheetListener,
        VoiceActionSheet.VoiceActionSheetListener,
        LiveBottomButtonLayout.LiveBottomButtonListener,
        TextView.OnEditorActionListener {

    private static final String TAG = LiveRoomActivity.class.getSimpleName();
    private static final int IDEAL_MIN_KEYBOARD_HEIGHT = 100;

    private View mContentView;
    private int mInputMethodHeight;

    // UI components of a live room
    protected LiveRoomParticipantLayout participants;
    protected LiveRoomMessageList messageList;
    protected LiveBottomButtonLayout bottomButtons;
    protected LiveMessageEditLayout messageEditLayout;
    protected AppCompatEditText mMessageEditText;
    protected Dialog curDialog;

    // values of a live room
    protected String roomName;
    protected boolean isOwner;
    protected boolean isHost;

    protected InputMethodManager mInputMethodManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContentView = findViewById(Window.ID_ANDROID_CONTENT);
        mContentView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        detectKeyboardLayout();
                    }
                });

        initData();

        mInputMethodManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    private void detectKeyboardLayout() {
        Rect rect = new Rect();
        mContentView.getWindowVisibleDisplayFrame(rect);
        int diff = displayHeight - systemBarHeight - rect.height();

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

    private void initData() {
        Intent intent = getIntent();
        roomName = intent.getStringExtra(Global.Constants.KEY_ROOM_NAME);
        isOwner = intent.getBooleanExtra(Global.Constants.KEY_IS_ROOM_OWNER, false);
        isHost = isOwner;
    }

    @Override
    public void onBeautyEnabled(boolean enabled) {
        Log.i(TAG, "onBeautyEnabled:" + enabled);
        bottomButtons.setBeautyEnabled(enabled);
    }

    @Override
    public void onBrightnessSelected(float brightness) {
        Log.i(TAG, "onBrightnessSelected:" + brightness);
    }

    @Override
    public void onSmoothSelected(float smooth) {
        Log.i(TAG, "onSmoothSelected:" + smooth);
    }

    @Override
    public void onColorTemperatureSelected(float temperature) {
        Log.i(TAG, "onColorTemperatureSelected:" + temperature);
    }

    @Override
    public void onContrastSelected(int type) {
        Log.i(TAG, "onContrastSelected:" + type);
    }

    @Override
    public void onResolutionSelected(int index) {
        Log.i(TAG, "onResolutionSelected:" + index);
    }

    @Override
    public void onFrameRateSelected(int index) {
        Log.i(TAG, "onFrameRateSelected:" + index);
    }

    @Override
    public void onBitrateSelected(int bitrate) {
        Log.i(TAG, "onBitrateSelected:" + bitrate);
    }

    @Override
    public void onSettingBackPressed() {
        Log.i(TAG, "onSettingBackPressed:");
        dismissActionSheetDialog();
    }

    @Override
    public void onBackgroundMusicSelected(int index, String name, String url) {
        Log.i(TAG, "onBackgroundMusicSelected:" + name);
        bottomButtons.setMusicPlaying(true);
    }

    @Override
    public void onBackgroundMusicStopped() {
        Log.i(TAG, "onBackgroundMusicStopped:");
        bottomButtons.setMusicPlaying(false);
    }

    @Override
    public void onGiftSend(String name, String path, int value) {
        Log.i(TAG, "onGiftSend:" + name);
    }

    @Override
    public void onVoiceClicked() {
        Log.i(TAG, "onVoiceClicked");
        showActionSheetDialog(ACTION_SHEET_VOICE, isHost, false, this);
    }

    @Override
    public void onRealDataClicked() {
        Log.i(TAG, "onRealDataClicked");
    }

    @Override
    public void onShareClicked() {
        Log.i(TAG, "onShareClicked");
    }

    @Override
    public void onSettingClicked() {
        Log.i(TAG, "onSettingClicked");
        showActionSheetDialog(ACTION_SHEET_VIDEO, isHost, false, this);
    }

    @Override
    public void onRotateClicked() {
        Log.i(TAG, "onRotateClicked");
    }

    @Override
    public void onVideoClicked(boolean muted) {
        Log.i(TAG, "onVideoClicked:" + muted);
    }

    @Override
    public void onSpeakerClicked(boolean muted) {
        Log.i(TAG, "onSpeakerClicked:" + muted);
    }

    @Override
    public void onAudioRouteSelected(int type) {
        Log.i(TAG, "onAudioRouteSelected:" + type);
    }

    @Override
    public void onAudioRouteEnabled(boolean enabled) {
        Log.i(TAG, "onAudioRouteEnabled:" + enabled);
    }

    @Override
    public void onAudioBackPressed() {
        Log.i(TAG, "onAudioBackPressed");
        dismissActionSheetDialog();
    }

    @Override
    public void onShowMessageInput() {
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
                messageList.addMessage("me", message);
                mMessageEditText.setText("");
            }

            mInputMethodManager.hideSoftInputFromWindow(mMessageEditText.getWindowToken(), 0);
            return true;
        }
        return false;
    }
}
