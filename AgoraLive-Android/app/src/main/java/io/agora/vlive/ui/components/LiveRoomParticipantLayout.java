package io.agora.vlive.ui.components;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import java.io.IOException;

import io.agora.vlive.R;

public class LiveRoomParticipantLayout extends RelativeLayout {
    private static final int EXPAND_MAX = 4;

    private int mMaxWidth;
    private int mHeight;
    private int mIconSize;
    private int mIconMargin;
    private LinearLayout mIconLayout;

    public LiveRoomParticipantLayout(Context context) {
        super(context);
        init();
    }

    public LiveRoomParticipantLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LiveRoomParticipantLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mHeight = getResources().getDimensionPixelSize(R.dimen.live_name_pad_height);
        mIconSize = getResources().getDimensionPixelSize(R.dimen.live_participant_layout_height);
        mIconMargin = getResources().getDimensionPixelSize(R.dimen.live_participant_icon_margin);

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View layout = inflater.inflate(R.layout.live_participant_layout, this, true);
        mIconLayout = layout.findViewById(R.id.icon_layout);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        setMeasuredDimension(width, mHeight);
        int heightSpec = MeasureSpec.makeMeasureSpec(mHeight, MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightSpec);
    }

    /**
     * For development only, test fake user icon
     * @param name
     */
    public void setIconResource(String name) {
        RoundedBitmapDrawable drawable = null;
        try {
            drawable = RoundedBitmapDrawableFactory.create(getResources(),
                    getResources().getAssets().open(name));
            drawable.setCircular(true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        AppCompatImageView imageView = new AppCompatImageView(getContext());
        mIconLayout.addView(imageView, mIconSize, mIconSize);
        LinearLayout.LayoutParams params =
                (LinearLayout.LayoutParams) imageView.getLayoutParams();
        params.rightMargin = mIconMargin;
        imageView.setLayoutParams(params);

        imageView.setImageDrawable(drawable);
    }
}
