package io.agora.vlive.ui.live;

import android.content.Intent;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatTextView;

import io.agora.vlive.Global;
import io.agora.vlive.R;
import io.agora.vlive.camera.capture.CameraCapture;
import io.agora.vlive.ui.BaseActivity;

public class LivePrepareActivity extends BaseActivity implements View.OnClickListener {
    private SurfaceView mSurfaceView;
    private CameraCapture mCameraCapture = new CameraCapture();

    private AppCompatEditText mEditText;
    private AppCompatTextView mStartBroadBtn;
    private int roomType;

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

    private void initUI() {
        mEditText = findViewById(R.id.room_name_edit);
        mStartBroadBtn = findViewById(R.id.start_broadcast_btn);
        mStartBroadBtn.setOnClickListener(this);
        findViewById(R.id.prepare_beauty_btn).setOnClickListener(this);
        findViewById(R.id.prepare_setting_btn).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.random_btn:
                generateRandomRoomName();
                break;
            case R.id.start_broadcast_btn:
                gotoBroadcastActivity();
                break;
            case R.id.prepare_beauty_btn:
                showActionSheetDialog(ACTION_SHEET_BEAUTY);
                break;
            case R.id.prepare_setting_btn:
                showActionSheetDialog(ACTION_SHEET_VIDEO);
                break;
        }
    }

    private void generateRandomRoomName() {

    }

    private void gotoBroadcastActivity() {
        Intent intent;
        switch (roomType) {
            case Global.Constants.TAB_ID_SINGLE:
                intent = new Intent(this, SingleHostActivity.class);
                break;
            case Global.Constants.TAB_ID_PK:
                intent = new Intent(this, HostPKLiveActivity.class);
                break;
            default:
                intent = new Intent(this, HostInLiveActivity.class);
                break;
        }

        intent.putExtras(getIntent());
        intent.putExtra(Global.Constants.KEY_ROOM_NAME, "");
        startActivity(intent);
    }
}
