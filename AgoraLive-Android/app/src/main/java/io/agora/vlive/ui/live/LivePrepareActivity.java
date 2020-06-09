package io.agora.vlive.ui.live;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
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
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import org.jetbrains.annotations.NotNull;

import io.agora.capture.video.camera.VideoModule;
import io.agora.capture.video.camera.CameraVideoChannel;
import io.agora.framework.PreprocessorFaceUnity;
import io.agora.framework.modules.channels.ChannelManager;
import io.agora.framework.modules.consumers.TextureViewConsumer;
import io.agora.vlive.Config;
import io.agora.vlive.ui.actionsheets.BeautySettingActionSheet;
import io.agora.vlive.ui.actionsheets.LiveRoomSettingActionSheet;
import io.agora.vlive.utils.Global;
import io.agora.vlive.R;
import io.agora.vlive.utils.RandomUtil;

public class LivePrepareActivity extends LiveBaseActivity implements View.OnClickListener, TextWatcher,
        BeautySettingActionSheet.BeautyActionSheetListener,
        LiveRoomSettingActionSheet.LiveRoomSettingActionSheetListener {

    public static final int RESULT_GO_LIVE = 2;

    private static final String TAG = LivePrepareActivity.class.getSimpleName();
    private static final int MAX_NAME_LENGTH = 25;

    private AppCompatTextView mStartBroadBtn;
    private AppCompatImageView mCloseBtn;
    private AppCompatImageView mSwitchBtn;
    private AppCompatImageView mRandomBtn;
    private RelativeLayout mEditLayout;
    private AppCompatTextView mEditHint;
    private AppCompatEditText mEditText;
    private AppCompatImageView mBeautyBtn;
    private AppCompatImageView mSettingBtn;
    private Dialog mExitDialog;

    private int roomType;
    private String mNameTooLongToastMsg;

    private CameraVideoChannel mCameraChannel;
    private PreprocessorFaceUnity mPreprocessor;

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
        setRandomRoomName();

        mNameTooLongToastMsg = String.format(getResources().getString(
                R.string.live_prepare_name_too_long_toast_format), MAX_NAME_LENGTH);

        mStartBroadBtn = findViewById(R.id.live_room_action_sheet_gift_send_btn);
        mCloseBtn = findViewById(R.id.live_prepare_close);
        mSwitchBtn = findViewById(R.id.live_prepare_switch_camera);
        mRandomBtn = findViewById(R.id.random_btn);
        mEditLayout = findViewById(R.id.prepare_name_edit_layout);
        mEditHint = findViewById(R.id.room_name_edit_hint);
        mBeautyBtn = findViewById(R.id.live_prepare_beauty_btn);
        mSettingBtn = findViewById(R.id.live_prepare_setting_btn);

        mStartBroadBtn.setOnClickListener(this);
        mRandomBtn.setOnClickListener(this);
        mCloseBtn.setOnClickListener(this);
        mSwitchBtn.setOnClickListener(this);
        mBeautyBtn.setOnClickListener(this);
        mSettingBtn.setOnClickListener(this);

        mCameraChannel = (CameraVideoChannel) VideoModule.instance().
                getVideoChannel(ChannelManager.ChannelID.CAMERA);
        mPreprocessor = (PreprocessorFaceUnity) VideoModule.instance().
                getPreprocessor(ChannelManager.ChannelID.CAMERA);

        changeUIStyles();

        FrameLayout localPreviewLayout = findViewById(R.id.local_preview_layout);
        TextureView textureView = new TextureView(this);
        TextureViewConsumer consumer = new TextureViewConsumer();
        textureView.setSurfaceTextureListener(consumer);
        localPreviewLayout.addView(textureView);

        startCaptureIfStopped();
    }

    private void changeUIStyles() {
        if (tabId == Config.LIVE_TYPE_VIRTUAL_HOST) {
            // It only accepts front camera frames for virtual images.
            mSwitchBtn.setVisibility(View.GONE);
            mCloseBtn.setImageResource(R.drawable.icon_back_black);
            mRandomBtn.setImageResource(R.drawable.random_button_black);
            mEditHint.setTextColor(getResources().getColor(R.color.gray_alpha2));
            mEditText.setTextColor(getResources().getColor(android.R.color.black));
            mEditLayout.setBackgroundResource(R.drawable.room_edit_layout_bg_gray);
            RelativeLayout layout = findViewById(R.id.activity_layout);
            layout.setBackgroundColor(Color.WHITE);
            mBeautyBtn.setVisibility(View.GONE);
            mSettingBtn.setVisibility(View.GONE);
            int virtualImage = getIntent().getIntExtra(Global.Constants.KEY_VIRTUAL_IMAGE, -1);
            mPreprocessor.onAnimojiSelected(virtualImage);
        } else {
            mCloseBtn.setImageResource(R.drawable.close_button_white);
            mSwitchBtn.setImageResource(R.drawable.switch_camera_white);
            mRandomBtn.setImageResource(R.drawable.random_button_white);
            mEditHint.setTextColor(getResources().getColor(R.color.gray_lightest));
            mEditText.setTextColor(getResources().getColor(android.R.color.white));
            mEditLayout.setBackgroundResource(R.drawable.room_edit_layout_bg_dark_gray);
            mPreprocessor.onAnimojiSelected(-1);
        }
    }

    @Override
    protected void onPermissionGranted() {
        roomType = getIntent().getIntExtra(Global.Constants.TAB_KEY, Global.Constants.TAB_ID_MULTI);
        mPermissionGranted = true;
        initUI();
    }

    private void startCaptureIfStopped() {
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
                setRandomRoomName();
                break;
            case R.id.live_room_action_sheet_gift_send_btn:
                gotoBroadcastActivity();
                break;
            case R.id.live_prepare_beauty_btn:
                showActionSheetDialog(ACTION_SHEET_BEAUTY, tabIdToLiveType(tabId), true, true, this);
                break;
            case R.id.live_prepare_setting_btn:
                showActionSheetDialog(ACTION_SHEET_VIDEO, tabIdToLiveType(tabId), true, true, this);
                break;
        }
    }

    private void setRandomRoomName() {
        mEditText.setText(RandomUtil.randomLiveRoomName(this));
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
            case Config.LIVE_TYPE_SINGLE_HOST:
                intent = new Intent(this, SingleHostLiveActivity.class);
                break;
            case Config.LIVE_TYPE_PK_HOST:
                intent = new Intent(this, HostPKLiveActivity.class);
                break;
            case Config.LIVE_TYPE_MULTI_HOST:
                intent = new Intent(this, MultiHostLiveActivity.class);
                break;
            case Config.LIVE_TYPE_VIRTUAL_HOST:
                intent = new Intent(this, VirtualHostLiveActivity.class);
                break;
            default: return;
        }

        if (getIntent().getExtras() != null) {
            intent.putExtras(getIntent().getExtras());
        }

        intent.putExtra(Global.Constants.KEY_ROOM_NAME, mEditText.getText().toString());
        startActivity(intent);
        // If we go live, we send a message to image select
        // activity that it does need to keep track in stack
        setResult(RESULT_GO_LIVE);
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
    public void onActionSheetBlurSelected(float blur) {
        Log.d(TAG, "onActionSheetBlurSelected:" + blur);
        setBlurValue(blur);
    }

    @Override
    public void onActionSheetWhitenSelected(float whiten) {
        Log.d(TAG, "onActionSheetWhitenSelected:" + whiten);
        setWhitenValue(whiten);
    }

    @Override
    public void onActionSheetCheekSelected(float cheek) {
        Log.d(TAG, "onActionSheetCheekSelected:" + cheek);
        setCheekValue(cheek);
    }

    @Override
    public void onActionSheetEyeEnlargeSelected(float eye) {
        Log.d(TAG, "onActionSheetEyeEnlargeSelected:" + eye);
        setEyeValue(eye);
    }

    @Override
    public void onActionSheetResolutionSelected(int index) {
        Log.d(TAG, "onActionSheetResolutionSelected:" + index);
        config().setResolutionIndex(index);
    }

    @Override
    public void onActionSheetFrameRateSelected(int index) {
        Log.d(TAG, "onActionSheetFrameRateSelected:" + index);
        config().setFrameRateIndex(index);
    }

    @Override
    public void onActionSheetBitrateSelected(int bitrate) {
        Log.d(TAG, "onActionSheetBitrateSelected:" + bitrate);
        config().setVideoBitrate(bitrate);
    }

    @Override
    public void onActionSheetSettingBackPressed() {
        Log.d(TAG, "onActionSheetSettingBackPressed:");
    }

    @Override
    public void onBackPressed() {
        boolean fromVirtualImage = getIntent().getBooleanExtra(
                VirtualImageSelectActivity.
                KEY_FROM_VIRTUAL_IMAGE, false);

        if (fromVirtualImage) {
            finish();
            return;
        }

        mExitDialog = showDialog(R.string.finish_broadcast_title_owner,
                R.string.finish_broadcast_message_owner, view -> {
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
            mPreprocessor.onAnimojiSelected(-1);
            mCameraChannel.stopCapture();
        }
    }
}
