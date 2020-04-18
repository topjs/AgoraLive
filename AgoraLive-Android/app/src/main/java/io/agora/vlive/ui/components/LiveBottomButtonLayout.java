package io.agora.vlive.ui.components;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import io.agora.vlive.AgoraLiveApplication;
import io.agora.vlive.R;

public class LiveBottomButtonLayout extends RelativeLayout implements View.OnClickListener {
    public static final int ROLE_AUDIENCE = 1;
    public static final int ROLE_HOST = 2;
    public static final int ROLE_OWNER = 3;

    public interface LiveBottomButtonListener {
        void onLiveBottomLayoutShowMessageEditor();
    }

    private int mHeight;
    private int mIconSize;
    private int mIconMargin;

    private AppCompatImageView mCloseBtn;
    private AppCompatImageView mMoreBtn;
    private AppCompatImageView mFun1;
    private AppCompatImageView mFun2;
    private AppCompatTextView mInputText;
    private int mRole;

    private LiveBottomButtonListener mListener;

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

        LayoutInflater.from(getContext()).inflate(
                R.layout.live_bottom_button_layout, this, true);

        mCloseBtn = findViewById(R.id.live_bottom_btn_close);
        mMoreBtn = findViewById(R.id.live_bottom_btn_more);
        mFun1 = findViewById(R.id.live_bottom_btn_fun1);
        mFun2 = findViewById(R.id.live_bottom_btn_fun2);
        mInputText = findViewById(R.id.live_bottom_message_input_hint);
        mInputText.setOnClickListener(this);
    }

    public void setRole(int role) {
        mRole = role;

        if (mRole == ROLE_OWNER) {
            mFun1.setImageResource(R.drawable.live_bottom_button_music);
            mFun2.setVisibility(View.VISIBLE);
            mFun2.setImageResource(R.drawable.live_bottom_button_beauty);
        } else if (mRole == ROLE_HOST) {
            mFun2.setVisibility(View.VISIBLE);
            mFun2.setImageResource(R.drawable.live_bottom_button_beauty);
            mFun1.setImageResource(R.drawable.live_bottom_btn_present);
        } else if (mRole == ROLE_AUDIENCE) {
            mFun2.setVisibility(View.GONE);
            mFun1.setImageResource(R.drawable.live_bottom_btn_present);
        }
    }

    public void setLiveBottomButtonListener(LiveBottomButtonListener listener) {
        mListener = listener;
    }

    public void setMusicPlaying(boolean playing) {
        if (mRole == ROLE_OWNER) mFun1.setActivated(playing);
    }

    public void setBeautyEnabled(boolean enabled) {
        if (mRole == ROLE_OWNER || mRole == ROLE_HOST) mFun2.setActivated(enabled);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        setMeasuredDimension(width, mHeight);
        int heightSpec = MeasureSpec.makeMeasureSpec(mHeight, MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightSpec);
    }

    /**
     * Clear all global settings that are no longer needed
     * after leaving the live room
     * @param application
     */
    public void clearStates(AgoraLiveApplication application) {
        application.config().setCurrentMusicIndex(-1);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.live_bottom_message_input_hint) {
            if (mListener != null) mListener.onLiveBottomLayoutShowMessageEditor();
        }
    }
}
