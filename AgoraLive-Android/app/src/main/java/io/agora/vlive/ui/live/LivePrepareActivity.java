package io.agora.vlive.ui.live;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatTextView;

import org.jetbrains.annotations.NotNull;

import io.agora.framework.VideoModule;
import io.agora.framework.channels.CameraVideoChannel;
import io.agora.framework.channels.ChannelManager;
import io.agora.framework.comsumers.TextureViewConsumer;
import io.agora.vlive.ui.actionsheets.BeautySettingActionSheet;
import io.agora.vlive.ui.actionsheets.LiveRoomSettingActionSheet;
import io.agora.vlive.utils.Global;
import io.agora.vlive.R;

public class LivePrepareActivity extends LiveBaseActivity implements View.OnClickListener, TextWatcher,
        BeautySettingActionSheet.BeautyActionSheetListener,
        LiveRoomSettingActionSheet.LiveRoomSettingActionSheetListener {

    private static final String TAG = LivePrepareActivity.class.getSimpleName();
    private static final int MAX_NAME_LENGTH = 25;

    private AppCompatEditText mEditText;
    private AppCompatTextView mStartBroadBtn;
    private Dialog mExitDialog;

    private int roomType;
    private String mNameTooLongToastMsg;

    private CameraVideoChannel mCameraChannel;

    // If camera is to persist, the camera capture is not
    // stopped, and we want to keep the capture and transit
    // to next activity.
    private boolean mCameraPersist;

    private boolean mActivityFinished;

    private boolean mPermissionGranted;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideStatusBar(false);
    }

    @Override
    protected void onGlobalLayoutCompleted() {

    }

    private void initUI() {
        hideStatusBar(false);
        setContentView(R.layout.activity_live_prepare);

        View topLayout = findViewById(R.id.prepare_top_btn_layout);
        RelativeLayout.LayoutParams params =
                (RelativeLayout.LayoutParams) topLayout.getLayoutParams();
        params.topMargin += systemBarHeight;
        topLayout.setLayoutParams(params);

        mEditText = findViewById(R.id.room_name_edit);
        mEditText.addTextChangedListener(this);

        mNameTooLongToastMsg = String.format(getResources().getString(
                R.string.live_prepare_name_too_long_toast_format), MAX_NAME_LENGTH);

        mStartBroadBtn = findViewById(R.id.live_room_action_sheet_gift_send_btn);
        mStartBroadBtn.setOnClickListener(this);

        findViewById(R.id.live_prepare_beauty_btn).setOnClickListener(this);
        findViewById(R.id.live_prepare_setting_btn).setOnClickListener(this);
        findViewById(R.id.live_prepare_close).setOnClickListener(this);
        findViewById(R.id.live_prepare_switch_camera).setOnClickListener(this);

        FrameLayout localPreviewLayout = findViewById(R.id.local_preview_layout);
        TextureView textureView = new TextureView(this);
        TextureViewConsumer consumer = new TextureViewConsumer(VideoModule.instance());
        textureView.setSurfaceTextureListener(consumer);
        localPreviewLayout.addView(textureView);

        startCaptureIfStopped();
    }

    @Override
    protected void onPermissionGranted() {
        roomType = getIntent().getIntExtra(Global.Constants.TAB_KEY, Global.Constants.TAB_ID_MULTI);
        mPermissionGranted = true;
        initUI();
    }

    private void startCaptureIfStopped() {
        mCameraChannel = (CameraVideoChannel) VideoModule.instance().
                getVideoChannel(ChannelManager.ChannelID.CAMERA);
        if (mCameraChannel != null && !mCameraChannel.hasCaptureStarted()) {
            mCameraChannel.startCapture();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.live_prepare_close:
                onBackPressed();
                break;
            case R.id.live_prepare_switch_camera:
                switchCamera();
                break;
            case R.id.random_btn:
                generateRandomRoomName();
                break;
            case R.id.live_room_action_sheet_gift_send_btn:
                gotoBroadcastActivity();
                break;
            case R.id.live_prepare_beauty_btn:
                showActionSheetDialog(ACTION_SHEET_BEAUTY, true, true, this);
                break;
            case R.id.live_prepare_setting_btn:
                showActionSheetDialog(ACTION_SHEET_VIDEO, true, true, this);
                break;
        }
    }

    private void generateRandomRoomName() {

    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(@NotNull Editable editable) {
        if (editable.length() > MAX_NAME_LENGTH) {
            showShortToast(mNameTooLongToastMsg);
            mEditText.setText(editable.subSequence(0, MAX_NAME_LENGTH));
            mEditText.setSelection(MAX_NAME_LENGTH);
        }
    }

    private boolean isRoomNameValid() {
        return mEditText.getText() != null && !TextUtils.isEmpty(mEditText.getText());
    }

    private void gotoBroadcastActivity() {
        if (!isRoomNameValid()) {
            showShortToast(getResources().getString(R.string.live_prepare_no_room_name));
            return;
        }

        mStartBroadBtn.setEnabled(false);

        Intent intent;
        switch (roomType) {
            case Global.Constants.TAB_ID_SINGLE:
                intent = new Intent(this, SingleHostLiveActivity.class);
                break;
            case Global.Constants.TAB_ID_PK:
                intent = new Intent(this, HostPKLiveActivity.class);
                break;
            default:
                intent = new Intent(this, MultiHostLiveActivity.class);
                break;
        }

        if (getIntent().getExtras() != null) {
            intent.putExtras(getIntent().getExtras());
        }

        intent.putExtra(Global.Constants.KEY_ROOM_NAME, mEditText.getText().toString());
        startActivity(intent);
        mCameraPersist = true;
        finish();
    }

    @Override
    public void onActionSheetBeautyEnabled(boolean enabled) {
        Log.i(TAG, "onActionSheetBeautyEnabled:" + enabled);
        findViewById(R.id.live_prepare_beauty_btn).setActivated(enabled);
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
    }

    @Override
    public void onBackPressed() {
        mExitDialog = showDialog(R.string.finish_broadcast_title,
                R.string.finish_broadcast_message, view -> {
                    dismissDialog();
                    finish();
                });
    }

    private void dismissDialog() {
        if (mExitDialog != null && mExitDialog.isShowing()) {
            mExitDialog.dismiss();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mPermissionGranted) {
            startCaptureIfStopped();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (!mCameraPersist && mCameraChannel != null && !mActivityFinished
                && mCameraChannel.hasCaptureStarted()) {
            // Stop camera capture when this activity
            // goes to background, but not because
            // of being finished.
            mCameraChannel.stopCapture();
        }
    }

    @Override
    public void finish() {
        super.finish();
        dismissDialog();
        mActivityFinished = true;
        if (!mCameraPersist && mCameraChannel != null
                && mCameraChannel.hasCaptureStarted()) {
            mCameraChannel.stopCapture();
        }
    }
}
