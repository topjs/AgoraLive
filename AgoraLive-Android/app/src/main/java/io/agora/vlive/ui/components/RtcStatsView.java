package io.agora.vlive.ui.components;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

import io.agora.vlive.R;

public class RtcStatsView extends LinearLayout {
    private static final float LINE_EXTRA = 10f;
    private static final float LINE_MULTIPLY = 1f;

    private AppCompatTextView mTextView;
    private String mStatsFormat;

    public RtcStatsView(Context context) {
        super(context);
        init();
    }

    public RtcStatsView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setOrientation(LinearLayout.VERTICAL);
        mTextView = new AppCompatTextView(getContext());
        mTextView.setLineSpacing(LINE_EXTRA, LINE_MULTIPLY);
        addView(mTextView);
        mStatsFormat = getResources().getString(R.string.rtc_stats_format);
    }

    public void setLocalStats(float rxRate, float rxLoss, float txRate, float txLoss) {
        String stats = String.format(mStatsFormat, rxRate, rxLoss, txRate, txLoss);
        mTextView.setText(stats);
    }

    public void setRemoteStats() {

    }

}
