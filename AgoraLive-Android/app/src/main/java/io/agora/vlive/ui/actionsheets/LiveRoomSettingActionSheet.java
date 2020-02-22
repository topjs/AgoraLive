package io.agora.vlive.ui.actionsheets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import io.agora.vlive.Global;
import io.agora.vlive.R;

public class LiveRoomSettingActionSheet extends RelativeLayout implements View.OnClickListener {
    private static final int PAGE_MAIN = 0;
    private static final int PAGE_RESOLUTION = 1;
    private static final int PAGE_FRAME_RATE = 2;

    private View mMain;
    private View mBackIcon;
    private TextView mTitle;
    private LayoutInflater mInflater;
    private LinearLayout mContainer;

    private int mPaddingHorizontal;
    private int mDividerHeight;

    public LiveRoomSettingActionSheet(Context context) {
        super(context);
        init();
    }

    public LiveRoomSettingActionSheet(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LiveRoomSettingActionSheet(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mPaddingHorizontal = getResources().getDimensionPixelSize(
                R.dimen.live_room_action_sheet_margin);
        mDividerHeight = getResources().getDimensionPixelSize(
                R.dimen.live_room_action_sheet_item_divider_height);
        mInflater = LayoutInflater.from(getContext());
        View layout = mInflater.inflate(
                R.layout.action_room_settings, this, false);
        addView(layout);

        mContainer = findViewById(R.id.live_room_setting_container);
        mMain = mInflater.inflate(R.layout.action_room_settings_main, this, false);
        mBackIcon = layout.findViewById(R.id.live_room_setting_back);
        mBackIcon.setOnClickListener(this);
        mTitle = layout.findViewById(R.id.live_room_action_sheet_bg_music_title);
        mMain.findViewById(R.id.live_room_setting_resolution).setOnClickListener(this);
        mMain.findViewById(R.id.live_room_setting_framerate).setOnClickListener(this);
        gotoMainPage();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.live_room_setting_back:
                gotoPage(PAGE_MAIN);
                break;
            case R.id.live_room_setting_resolution:
                gotoPage(PAGE_RESOLUTION);
                break;
            case R.id.live_room_setting_framerate:
                gotoPage(PAGE_FRAME_RATE);
                break;
        }
    }

    private void gotoPage(int page) {
        switch (page) {
            case PAGE_MAIN:
                gotoMainPage();
                break;
            case PAGE_RESOLUTION:
                gotoResolutionPage();
                break;
            case PAGE_FRAME_RATE:
                gotoFrameRatePage();
                break;
        }
    }

    private void gotoMainPage() {
        mBackIcon.setVisibility(View.GONE);
        mTitle.setText(R.string.live_room_setting_action_sheet_title);
        mContainer.removeAllViews();
        mContainer.addView(mMain);
        //TODO reset all texts
    }

    private void gotoResolutionPage() {
        mBackIcon.setVisibility(View.VISIBLE);
        mTitle.setText(R.string.live_room_setting_title_resolution);
        RecyclerView resolutionRecycler = new RecyclerView(getContext());
        LinearLayoutManager manager = new LinearLayoutManager(getContext(),
                LinearLayoutManager.VERTICAL, false);
        resolutionRecycler.setLayoutManager(manager);
        resolutionRecycler.setAdapter(new ResolutionAdapter());
        resolutionRecycler.addItemDecoration(new LineDecorator());
        mContainer.removeAllViews();
        mContainer.addView(resolutionRecycler);
    }

    private void gotoFrameRatePage() {
        mBackIcon.setVisibility(View.VISIBLE);
        mTitle.setText(R.string.live_room_setting_title_framerate);
        RecyclerView frameRateRecycler = new RecyclerView(getContext());
        LinearLayoutManager manager = new LinearLayoutManager(getContext(),
                LinearLayoutManager.VERTICAL, false);
        frameRateRecycler.setLayoutManager(manager);
        frameRateRecycler.setAdapter(new FrameRateAdapter());
        frameRateRecycler.addItemDecoration(new LineDecorator());
        mContainer.removeAllViews();
        mContainer.addView(frameRateRecycler);
    }

    private class ResolutionAdapter extends RecyclerView.Adapter {
        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ResolutionViewHolder(mInflater.inflate(
                    R.layout.live_room_setting_list_item_text_only,
                    parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ResolutionViewHolder resolutionHolder = (ResolutionViewHolder) holder;
            resolutionHolder.textView.setText(Global.Constants.RESOLUTIONS[position]);
        }

        @Override
        public int getItemCount() {
            return Global.Constants.RESOLUTIONS.length;
        }
    }

    private class ResolutionViewHolder extends RecyclerView.ViewHolder {
        AppCompatTextView textView;
        ResolutionViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.live_room_setting_item_text);
            itemView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });
        }
    }

    private class FrameRateAdapter extends RecyclerView.Adapter {
        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new FrameRateViewHolder(mInflater.inflate(
                    R.layout.live_room_setting_list_item_text_only,
                    parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            FrameRateViewHolder frameRateHolder = (FrameRateViewHolder) holder;
            frameRateHolder.textView.setText(Global.Constants.FRAME_RATES[position]);
        }

        @Override
        public int getItemCount() {
            return Global.Constants.FRAME_RATES.length;
        }
    }

    private class FrameRateViewHolder extends RecyclerView.ViewHolder {
        AppCompatTextView textView;
        FrameRateViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.live_room_setting_item_text);
            itemView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });
        }
    }

    private class LineDecorator extends RecyclerView.ItemDecoration {
        @Override
        public void onDrawOver(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            Rect rect = new Rect();
            Paint paint = new Paint();
            paint.setColor(Global.Constants.DIVIDER_COLOR);

            int count = parent.getChildCount();
            for (int i = 0; i < count - 1; i++) {
                View child = parent.getChildAt(i);
                child.getDrawingRect(rect);
                int startX = rect.left + mPaddingHorizontal;
                int width = rect.right - rect.left - startX * 2;
                int height = rect.bottom - rect.top;
                int startY = height * (i + 1);
                c.drawRect(new Rect(startX, startY,
                        startX + width, startY + mDividerHeight), paint);
            }
        }
    }
}
