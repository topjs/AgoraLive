package io.agora.vlive.ui.live;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatTextView;

import org.jetbrains.annotations.NotNull;

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

    private int roomType;
    private String mNameTooLongToastMsg;

    private TextureView mTextureView;
    private boolean mInitCalled;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideStatusBar(false);
        setContentView(R.layout.activity_live_prepare);
        roomType = getIntent().getIntExtra(Global.Constants.TAB_KEY, Global.Constants.TAB_ID_MULTI);
        initUI();
    }

    @Override
    protected void onGlobalLayoutCompleted() {
        View topLayout = findViewById(R.id.prepare_top_btn_layout);
        RelativeLayout.LayoutParams params =
                (RelativeLayout.LayoutParams) topLayout.getLayoutParams();
        params.topMargin += systemBarHeight;
        topLayout.setLayoutParams(params);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void initUI() {
        // onCreate() and onPermissionsGranted() may be called
        // in any order, and we want to call initUI() when the
        // last one of them is called.
        if (!mInitCalled) {
            mInitCalled = true;
            return;
        }

        mEditText = findViewById(R.id.room_name_edit);
        mEditText.addTextChangedListener(this);

        mNameTooLongToastMsg = String.format(getResources().getString(
                R.string.live_prepare_name_too_long_toast_format), MAX_NAME_LENGTH);

        mStartBroadBtn = findViewById(R.id.live_room_action_sheet_gift_send_btn);
        mStartBroadBtn.setOnClickListener(this);

        findViewById(R.id.live_prepare_beauty_btn).setOnClickListener(this);
        findViewById(R.id.live_prepare_setting_btn).setOnClickListener(this);
        findViewById(R.id.live_prepare_close).setOnClickListener(this);
        findViewById(R.id.live_prepare_rotate_camera).setOnClickListener(this);

        mTextureView = findViewById(R.id.live_prepare_texture_view);
        mTextureView.setVisibility(View.VISIBLE);
        cameraProxy().setRenderView(mTextureView);
    }

    @Override
    protected void onPermissionGranted() {
        initUI();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.live_prepare_close:
                finish();
                break;
            case R.id.live_prepare_rotate_camera:
                rotateCamera();
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

    private void rotateCamera() {

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
                intent = new Intent(this, HostInLiveActivity.class);
                break;
        }

        intent.putExtras(getIntent());
        intent.putExtra(Global.Constants.KEY_ROOM_NAME, mEditText.getText().toString());
        startActivity(intent);
    }

    @Override
    public void onBeautyEnabled(boolean enabled) {
        Log.i(TAG, "onBeautyEnabled:" + enabled);
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
    }
}
