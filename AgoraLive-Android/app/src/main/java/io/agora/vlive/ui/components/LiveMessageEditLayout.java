package io.agora.vlive.ui.components;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethod;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;

import androidx.appcompat.widget.AppCompatEditText;

import io.agora.vlive.R;

public class LiveMessageEditLayout extends RelativeLayout {
    public static final int EDIT_TEXT_ID = 1 << 4;
    private static final int HINT_TEXT_COLOR = Color.rgb(196, 196, 196);
    private static final int TEXT_COLOR = Color.rgb(239, 239, 239);
    private static final int TEXT_SIZE = 14;

    private AppCompatEditText mEditText;
    private int mMargin;
    private int mPadding;

    public LiveMessageEditLayout(Context context) {
        super(context);
        init();
    }

    public LiveMessageEditLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LiveMessageEditLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mMargin = getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin);
        mPadding = getResources().getDimensionPixelSize(R.dimen.live_bottom_edit_padding);

        mEditText = new AppCompatEditText(getContext());
        mEditText.setId(EDIT_TEXT_ID);
        LayoutParams params = new LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT);
        params.setMarginStart(mMargin);
        params.setMarginEnd(mMargin);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        addView(mEditText, params);

        mEditText.setPadding(mPadding, 0, mPadding, 0);
        mEditText.setHint(R.string.live_bottom_edit_hint);
        mEditText.setHintTextColor(HINT_TEXT_COLOR);
        mEditText.setTextColor(TEXT_COLOR);
        mEditText.setTextSize(TEXT_SIZE);
        mEditText.setSingleLine();
        mEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        mEditText.setBackgroundResource(R.drawable.round_scalable_gray_bg);
    }
}
