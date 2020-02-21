package io.agora.vlive.ui.actionsheets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import androidx.appcompat.widget.AppCompatTextView;

import io.agora.vlive.R;

public class BeautySettingActionSheet extends RelativeLayout implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {
    private AppCompatTextView mContrastLowBtn;
    private AppCompatTextView mContrastMediumBtn;
    private AppCompatTextView mContrastHighBtn;
    private View mBeautySwitch;

    public BeautySettingActionSheet(Context context) {
        super(context);
        init();
    }

    public BeautySettingActionSheet(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BeautySettingActionSheet(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        addView(LayoutInflater.from(getContext()).
                inflate(R.layout.action_beauty, this, false));
        ((SeekBar) findViewById(R.id.beauty_brightness_progress_bar)).setOnSeekBarChangeListener(this);
        ((SeekBar) findViewById(R.id.beauty_smooth_progress_bar)).setOnSeekBarChangeListener(this);
        ((SeekBar) findViewById(R.id.beauty_temperature_progress_bar)).setOnSeekBarChangeListener(this);

        mContrastLowBtn = findViewById(R.id.beauty_contrast_low);
        mContrastMediumBtn = findViewById(R.id.beauty_contrast_medium);
        mContrastHighBtn = findViewById(R.id.beauty_contrast_high);
        mContrastLowBtn.setOnClickListener(this);
        mContrastMediumBtn.setOnClickListener(this);
        mContrastHighBtn.setOnClickListener(this);

        mBeautySwitch = findViewById(R.id.beauty_switch);
        mBeautySwitch.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        mContrastLowBtn.setActivated(false);
        mContrastMediumBtn.setActivated(false);
        mContrastHighBtn.setActivated(false);

        switch (view.getId()) {
            case R.id.beauty_contrast_low:
                mContrastLowBtn.setActivated(true);
                break;
            case R.id.beauty_contrast_medium:
                mContrastMediumBtn.setActivated(true);
                break;
            case R.id.beauty_contrast_high:
                mContrastHighBtn.setActivated(true);
                break;
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
