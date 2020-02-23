package io.agora.vlive.ui.main.fragments;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.lang.ref.SoftReference;
import java.lang.reflect.Array;

import io.agora.vlive.utils.Global;
import io.agora.vlive.R;
import io.agora.vlive.ui.live.LivePrepareActivity;

public class RoomFragment extends AbstractFragment implements View.OnClickListener {
    private static final int TAB_COUNT = 3;
    private static final int TAB_TEXT_VIEW_INDEX = 1;

    private TabLayout mTabLayout;
    private ViewPager2 mViewPager;
    private int mCurrentTap;

    private String[] mTabTitles = new String[TAB_COUNT];

    @SuppressWarnings("unchecked")
    private SoftReference<TextView>[] mTabTexts =
            (SoftReference<TextView>[]) Array.newInstance(SoftReference.class, TAB_COUNT);

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("room", "onCreate: savedinstance:" + savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        if (bundle != null) {
            mCurrentTap = bundle.getInt(Global.Constants.TAB_KEY);
        } else {
            mCurrentTap = application().states().lastTabPosition();
        }

        View view = inflater.inflate(R.layout.fragment_room, container, false);
        getTabTitles();
        mTabLayout = view.findViewById(R.id.room_tab_layout);

        mViewPager = view.findViewById(R.id.room_list_pager);
        mViewPager.setAdapter(new RoomAdapter(this));

        mViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                Log.i("room", "page selected:" + position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
            }
        });

        new TabLayoutMediator(mTabLayout, mViewPager,
            new TabLayoutMediator.TabConfigurationStrategy() {
                @Override public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                    tab.setText(mTabTitles[position]);
                }
            }).attach();

        TabLayout.Tab tab = mTabLayout.getTabAt(mCurrentTap);
        if (tab != null) {
            mTabLayout.selectTab(tab, true);
            application().states().setLastTabPosition(mCurrentTap);
            setTextViewBold(getCachedTabText(tab), true);
        }

        // Note tab selected listener should be set after
        // tab layout and view pager are attached together.
        // Before that, it cannot know how many tabs
        // are there.
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mCurrentTap = tab.getPosition();
                application().states().setLastTabPosition(mCurrentTap);
                setTextViewBold(getCachedTabText(tab), true);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                setTextViewBold(getCachedTabText(tab), false);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        view.findViewById(R.id.start_broadcast_btn).setOnClickListener(this);

        return view;
    }

    private void getTabTitles() {
        mTabTitles = new String[TAB_COUNT];
        for (int i = 0; i < TAB_COUNT; i++) {
            mTabTitles[i] = getResources().getString(Global.Constants.TAB_IDS_RES[i]);
        }
    }

    private TextView findTabTextView(@NonNull TabLayout.Tab tab) {
        View view = tab.view.getChildAt(TAB_TEXT_VIEW_INDEX);
        return view == null ? null :
                view instanceof TextView ? (TextView) view : null;
    }

    private TextView getCachedTabText(@NonNull TabLayout.Tab tab) {
        int position = tab.getPosition();
        if (position < 0 || position >= TAB_COUNT) return null;

        if (mTabTexts[position] == null || mTabTexts[position].get() == null) {
            mTabTexts[position] = new SoftReference<>(findTabTextView(tab));
        }

        return mTabTexts[position].get();
    }

    private void setTextViewBold(TextView view, boolean bold) {
        if (view == null) return;
        Typeface typeface = bold ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT;
        view.setTypeface(typeface);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.start_broadcast_btn) {
            Intent intent = new Intent(getActivity(), LivePrepareActivity.class);
            intent.putExtra(Global.Constants.TAB_KEY, mCurrentTap);
            intent.putExtra(Global.Constants.KEY_IS_HOST, true);
            startActivity(intent);
        }
    }

    private class RoomAdapter extends FragmentStateAdapter {
        RoomAdapter(@NonNull Fragment fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 1: return new SingleHostFragment();
                case 2: return new PKHostInFragment();
                default: return new HostInFragment();
            }
        }

        @Override
        public int getItemCount() {
            return TAB_COUNT;
        }
    }
}
