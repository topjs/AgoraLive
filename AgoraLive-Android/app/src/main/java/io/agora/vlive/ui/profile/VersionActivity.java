package io.agora.vlive.ui.profile;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import io.agora.vlive.R;
import io.agora.vlive.ui.BaseActivity;

public class VersionActivity extends BaseActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_version);
        hideStatusBar(true);
        initInfo();
    }

    @Override
    protected void onGlobalLayoutCompleted() {
        View topLayout = findViewById(R.id.activity_version_title_layout);
        if (topLayout != null) {
            LinearLayout.LayoutParams params =
                    (LinearLayout.LayoutParams)
                            topLayout.getLayoutParams();
            params.topMargin += systemBarHeight;
            topLayout.setLayoutParams(params);
        }
    }

    private void initInfo() {
        TextView textView = findViewById(R.id.agoralive_version_text);
        String versionTitle = getString(R.string.agoralive_version_title);
        String agoraLiveVersion = getAppVersion();
        SpannableString spannable = new SpannableString(versionTitle + agoraLiveVersion);
        spannable.setSpan(new ForegroundColorSpan(Color.BLACK),
                0, versionTitle.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        textView.setText(spannable);

        textView = findViewById(R.id.agoralive_release_time_text);
        String releaseTimeTitle = getString(R.string.agoralive_release_time_title);
        String releaseTime = getString(R.string.agoralive_release_time);
        spannable = new SpannableString(releaseTimeTitle + releaseTime);
        spannable.setSpan(new ForegroundColorSpan(Color.BLACK),
                0, releaseTimeTitle.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        textView.setText(spannable);

        textView = findViewById(R.id.media_sdk_version_text);
        String sdkVersionTitle = getString(R.string.agoralive_media_sdk_version_title);
        String sdkVersion = getString(R.string.agoralive_media_sdk_version);
        spannable = new SpannableString(sdkVersionTitle + sdkVersion);
        spannable.setSpan(new ForegroundColorSpan(Color.BLACK),
                0, sdkVersionTitle.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        textView.setText(spannable);
    }
}
