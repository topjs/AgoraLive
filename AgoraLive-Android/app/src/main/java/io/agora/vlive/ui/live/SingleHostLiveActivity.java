package io.agora.vlive.ui.live;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import io.agora.vlive.ui.actionsheets.BackgroundMusicActionSheet;
import io.agora.vlive.ui.actionsheets.BeautySettingActionSheet;
import io.agora.vlive.ui.actionsheets.LiveRoomSettingActionSheet;
import io.agora.vlive.utils.Global;
import io.agora.vlive.R;
import io.agora.vlive.ui.components.LiveBottomButtonLayout;
import io.agora.vlive.ui.components.LiveRoomMessageList;
import io.agora.vlive.ui.components.LiveHostNameLayout;
import io.agora.vlive.ui.components.LiveRoomParticipantLayout;

public class SingleHostLiveActivity extends BaseLiveActivity implements View.OnClickListener,
        BeautySettingActionSheet.BeautyActionSheetListener,
        LiveRoomSettingActionSheet.LiveRoomSettingActionSheetListener,
        BackgroundMusicActionSheet.BackgroundMusicActionSheetListener {
    private static final String TAG = SingleHostLiveActivity.class.getSimpleName();

    private LiveHostNameLayout mNamePad;
    private LiveRoomParticipantLayout mParticipants;
    private LiveRoomMessageList mMessageList;
    private LiveBottomButtonLayout mBottomButtons;
    private boolean mIsHost;

    private Dialog mDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_host);
        hideStatusBar(false);
        mIsHost = getIntent().getBooleanExtra(Global.Constants.KEY_IS_HOST, false);
        initUI();
    }

    private void initUI() {
        mNamePad = findViewById(R.id.single_live_name_pad);
        mNamePad.setName("康康有毒");
        mNamePad.setIconResource("fake_icon_1.jpeg");

        mParticipants = findViewById(R.id.single_live_participant);
        mParticipants.setIconResource("fake_icon_2.jpeg");
        mParticipants.setIconResource("fake_icon_3.jpeg");

        mMessageList = findViewById(R.id.message_list);
        mMessageList.addMessage("康康有毒", "他说会因为那一分钟而永远记住我，那时候我觉得很动听。但现在我看着时钟，我就告诉我自己，我要从这一分钟开始忘掉");
        mMessageList.addMessage("起司甜甜", "何必在乎其它人");
        mMessageList.notifyDataSetChanged();

        mBottomButtons = findViewById(R.id.single_live_bottom_layout);
        mBottomButtons.setHost(mIsHost);

        findViewById(R.id.live_bottom_btn_close).setOnClickListener(this);
        findViewById(R.id.live_bottom_btn_more).setOnClickListener(this);
        findViewById(R.id.live_bottom_btn_fun1).setOnClickListener(this);
        findViewById(R.id.live_bottom_btn_fun2).setOnClickListener(this);
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
                mDialog = showDialog(R.string.finish_broadcast_title,
                        R.string.finish_broadcast_message, this);
                break;
            case R.id.live_bottom_btn_more:
                break;
            case R.id.live_bottom_btn_fun1:
                if (mIsHost) {
                    showActionSheetDialog(ACTION_SHEET_BG_MUSIC, this);
                } else {

                }
                break;
            case R.id.live_bottom_btn_fun2:
                // this button is hidden when
                // current user is not host.
                if (mIsHost) {
                    showActionSheetDialog(ACTION_SHEET_BEAUTY, this);
                }
                break;
            case R.id.dialog_positive_button:
                closeDialog();
                finish();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        mDialog = showDialog(R.string.finish_broadcast_title,
                R.string.finish_broadcast_message, this);
    }

    private void closeDialog() {
        if (mDialog != null && mDialog.isShowing()) mDialog.dismiss();
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
    public void onBackgroundMusicSelected(int index, String name, String url) {
        Log.i(TAG, "onBackgroundMusicSelected:" + name);
    }
}
