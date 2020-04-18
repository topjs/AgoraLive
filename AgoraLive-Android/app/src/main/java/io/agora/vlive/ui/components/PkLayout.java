package io.agora.vlive.ui.components;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import io.agora.vlive.R;

public class PkLayout extends LinearLayout {
    private static final int TIMER_TICK_PERIOD = 1000;

    private AppCompatTextView mLeftPoint;
    private AppCompatTextView mRightPoint;
    private FrameLayout mLeftFrameLayout;
    private FrameLayout mRightFrameLayout;
    private AppCompatImageView mToOtherRoomBtn;
    private AppCompatTextView mRemainsText;
    private AppCompatTextView mOtherHostName;

    private long mTimerStopTimestamp;
    private Handler mTimerHandler;
    private CountDownRunnable mCountDownRunnable = new CountDownRunnable();

    private class CountDownRunnable implements Runnable {
        @Override
        public void run() {
            long current = System.currentTimeMillis();
            mRemainsText.setText(timestampToCountdown(mTimerStopTimestamp - current));
            mTimerHandler.postDelayed(mCountDownRunnable, TIMER_TICK_PERIOD);
        }
    }

    public PkLayout(Context context) {
        super(context);
        init();
    }

    public PkLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(
                R.layout.pk_video_layout, this, true);
        mLeftPoint = findViewById(R.id.pk_progress_left_text);
        mRightPoint = findViewById(R.id.pk_progress_right_text);
        mLeftFrameLayout = findViewById(R.id.pk_host_video_layout_left);
        mRightFrameLayout = findViewById(R.id.pk_host_video_layout_right);
        mToOtherRoomBtn = findViewById(R.id.pk_video_layout_enter_other_room_btn);
        mRemainsText = findViewById(R.id.pk_host_remaining_time_text);
        mOtherHostName = findViewById(R.id.pk_video_layout_other_host_name);
    }

    public void setHost(boolean isHost) {
        mToOtherRoomBtn.setVisibility(isHost ? View.GONE : View.VISIBLE);
    }

    public void setOnClickGotoPeerChannelListener(View.OnClickListener listener) {
        mToOtherRoomBtn.setOnClickListener(listener);
    }

    public void setPoints(int localPoint, int remotePoint) {
        if (localPoint < 0 || remotePoint < 0) {
            return;
        }

        int localWeight;
        int remoteWeight;
        if (localPoint == 0 && remotePoint == 0) {
            localWeight = 1;
            remoteWeight = 1;
        } else if (localPoint == 0) {
            localWeight = 5;
            remoteWeight = 95;
        } else if (remotePoint == 0) {
            localWeight = 95;
            remoteWeight = 5;
        } else {
            localWeight = localPoint;
            remoteWeight = remotePoint;
        }

        setWeight(mLeftPoint, localWeight);
        setWeight(mRightPoint, remoteWeight);

        mLeftPoint.setText(String.valueOf(localPoint));
        mRightPoint.setText(String.valueOf(remotePoint));
    }

    public void setWeight(AppCompatTextView textView, int weight) {
        LinearLayout.LayoutParams params =
                (LinearLayout.LayoutParams) textView.getLayoutParams();
        params.weight = weight;
        textView.setLayoutParams(params);
    }

    public FrameLayout getLeftVideoLayout() {
        return mLeftFrameLayout;
    }

    public FrameLayout getRightVideoLayout() {
        return mRightFrameLayout;
    }

    public void setOtherHostName(String name) {
        mOtherHostName.setText(name);
    }

    public void startCountDownTimer(long remaining) {
        mTimerStopTimestamp = System.currentTimeMillis() + remaining;
        mTimerHandler = new Handler(getContext().getMainLooper());
        mTimerHandler.post(() -> mRemainsText.setText(timestampToCountdown(remaining)));
        mTimerHandler.postDelayed(this::stopCountDownTimer, remaining);
        mTimerHandler.postDelayed(mCountDownRunnable, TIMER_TICK_PERIOD);
    }

    public void stopCountDownTimer() {
        if (mTimerHandler != null) {
            mTimerHandler.removeCallbacksAndMessages(null);
        }
    }

    public String timestampToCountdown(long remaining) {
        if (remaining <= 0) return "00:00";
        long seconds = remaining / 1000;
        long minute = seconds / 60;
        int remainSecond = (int) seconds % 60;
        String minuteString = minute < 10 ? "0" + minute : "" + minute;
        String secondString = remainSecond < 10 ? "0" + remainSecond : "" + remainSecond;
        return minuteString + ":" + secondString;
    }
}
