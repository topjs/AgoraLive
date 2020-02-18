package io.agora.vlive.ui.components;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;

import io.agora.vlive.R;

public class LiveBottomButtonLayout extends RelativeLayout {
    private int mHeight;
    private int mIconSize;
    private int mIconMargin;

    private AppCompatImageView mCloseBtn;
    private AppCompatImageView mMoreBtn;
    private AppCompatImageView mFun1;
    private AppCompatImageView mFun2;
    private AppCompatEditText mEditText;

    public LiveBottomButtonLayout(Context context) {
        super(context);
        init();
    }

    public LiveBottomButtonLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LiveBottomButtonLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mHeight = getResources().getDimensionPixelSize(R.dimen.live_bottom_layout_height);
        mIconMargin = getResources().getDimensionPixelSize(R.dimen.live_bottom_btn_margin);
        mIconSize = mHeight;

        LayoutInflater.from(getContext()).
                inflate(R.layout.live_bottom_button_layout,
                        this, true);

        mCloseBtn = findViewById(R.id.live_bottom_btn_close);
        mMoreBtn = findViewById(R.id.live_bottom_btn_more);
        mFun1 = findViewById(R.id.live_bottom_btn_fun1);
        mFun2 = findViewById(R.id.live_bottom_btn_fun2);
        mEditText = findViewById(R.id.live_bottom_text_edit);
    }

    public void setHost(boolean isHost) {
        if (isHost) {
            mFun1.setImageResource(R.drawable.live_bottom_button_music);
            mFun2.setImageResource(R.drawable.live_bottom_button_beauty);
        } else {
            mFun1.setImageResource(R.drawable.live_bottom_btn_present);
            // Android client does not have super resolution,
            // so hide this button for this moment.
            mFun2.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        setMeasuredDimension(width, mHeight);
        int heightSpec = MeasureSpec.makeMeasureSpec(mHeight, MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightSpec);
    }
}
