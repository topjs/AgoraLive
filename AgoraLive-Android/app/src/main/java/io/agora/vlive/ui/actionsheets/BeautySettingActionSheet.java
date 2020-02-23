package io.agora.vlive.ui.actionsheets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;

import androidx.appcompat.widget.AppCompatTextView;

import io.agora.vlive.R;

public class BeautySettingActionSheet extends AbstractActionSheet
        implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {
    public interface BeautyActionSheetListener extends AbsActionSheetListener {
        void onBeautyEnabled(boolean enabled);
        void onBrightnessSelected(float brightness);
        void onSmoothSelected(float smooth);
        void onColorTemperatureSelected(float temperature);
        void onContrastSelected(int type);
    }

    public static final int CONTRAST_LOW = 0;
    public static final int CONTRAST_MEDIUM = 1;
    public static final int CONTRAST_HIGH = 2;

    private SeekBar mBrightnessSeekBar;
    private SeekBar mSmoothSeekBar;
    private SeekBar mColorTempSeekBar;
    private AppCompatTextView mBrightnessValue;
    private AppCompatTextView mSmoothValue;
    private AppCompatTextView mColorTempValue;
    private AppCompatTextView mContrastLowBtn;
    private AppCompatTextView mContrastMediumBtn;
    private AppCompatTextView mContrastHighBtn;
    private View mBeautySwitch;

    private BeautyActionSheetListener mListener;

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

        mBrightnessSeekBar = findViewById(R.id.beauty_brightness_progress_bar);
        mSmoothSeekBar = findViewById(R.id.beauty_smooth_progress_bar);
        mColorTempSeekBar = findViewById(R.id.beauty_temperature_progress_bar);

        mBrightnessSeekBar.setOnSeekBarChangeListener(this);
        mSmoothSeekBar.setOnSeekBarChangeListener(this);
        mColorTempSeekBar.setOnSeekBarChangeListener(this);

        mBrightnessValue = findViewById(R.id.beauty_value_brightness);
        mSmoothValue = findViewById(R.id.beauty_value_smooth);
        mColorTempValue = findViewById(R.id.beauty_value_temperature);

        float value = application().states().beautyBrightness();
        mBrightnessSeekBar.setProgress(valueToProgress(value));
        mBrightnessValue.setText(String.valueOf(value));

        value = application().states().beautySmooth();
        mSmoothSeekBar.setProgress(valueToProgress(value));
        mSmoothValue.setText(String.valueOf(value));

        value = application().states().beautyColorTemp();
        mColorTempSeekBar.setProgress(valueToProgress(value));
        mColorTempValue.setText(String.valueOf(value));

        mContrastLowBtn = findViewById(R.id.beauty_contrast_low);
        mContrastMediumBtn = findViewById(R.id.beauty_contrast_medium);
        mContrastHighBtn = findViewById(R.id.beauty_contrast_high);
        mContrastLowBtn.setOnClickListener(this);
        mContrastMediumBtn.setOnClickListener(this);
        mContrastHighBtn.setOnClickListener(this);

        contrastTypeToButton(application().states().
                beautyContrast()).setActivated(true);

        mBeautySwitch = findViewById(R.id.beauty_switch);
        mBeautySwitch.setOnClickListener(this);
        mBeautySwitch.setActivated(application().states().isBeautyEnabled());
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (R.id.beauty_switch == id) {
            boolean activated = !mBeautySwitch.isActivated();
            mBeautySwitch.setActivated(activated);
            application().states().setBeautyEnabled(activated);
            if (mListener != null) mListener.onBeautyEnabled(activated);
            return;
        }

        mContrastLowBtn.setActivated(false);
        mContrastMediumBtn.setActivated(false);
        mContrastHighBtn.setActivated(false);

        int type = CONTRAST_LOW;
        switch (view.getId()) {
            case R.id.beauty_contrast_low:
                mContrastLowBtn.setActivated(true);
                type = CONTRAST_LOW;
                break;
            case R.id.beauty_contrast_medium:
                mContrastMediumBtn.setActivated(true);
                type = CONTRAST_MEDIUM;
                break;
            case R.id.beauty_contrast_high:
                mContrastHighBtn.setActivated(true);
                type = CONTRAST_HIGH;
                break;
        }

        application().states().setContrast(type);
        if (mListener != null) mListener.onContrastSelected(type);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        float value = progressToValue(seekBar.getProgress());
        switch (seekBar.getId()) {
            case R.id.beauty_brightness_progress_bar:
                mBrightnessValue.setText(String.valueOf(value));
                application().states().setBeautyBrightness(value);
                if (mListener != null) mListener.onBrightnessSelected(value);
                break;
            case R.id.beauty_smooth_progress_bar:
                mSmoothValue.setText(String.valueOf(value));
                application().states().setBeautySmooth(value);
                if (mListener != null) mListener.onSmoothSelected(value);
                break;
            case R.id.beauty_temperature_progress_bar:
                mColorTempValue.setText(String.valueOf(value));
                application().states().setBeautyColorTemp(value);
                if (mListener != null) mListener.onColorTemperatureSelected(value);
                break;
        }
    }

    private int valueToProgress(float value) {
        return (int) (value * 10);
    }

    private float progressToValue(int progress) {
        return progress / 10.0f;
    }

    private AppCompatTextView contrastTypeToButton(int type) {
        switch (type) {
            case CONTRAST_LOW:
                return mContrastLowBtn;
            case CONTRAST_MEDIUM:
                return mContrastMediumBtn;
            default:
                return mContrastHighBtn;
        }
    }

    @Override
    public void setActionSheetListener(AbsActionSheetListener listener) {
        if (listener instanceof BeautyActionSheetListener) {
            mListener = (BeautyActionSheetListener) listener;
        }
    }
}
