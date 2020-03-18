package io.agora.vlive.ui.main.fragments;

import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import io.agora.vlive.Config;
import io.agora.vlive.R;
import io.agora.vlive.utils.UserProfileUtil;

public class MeFragment extends AbstractFragment implements View.OnClickListener {
    private static final String TAG = MeFragment.class.getSimpleName();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Config.UserProfile profile = getContainer().config().getUserProfile();
        View layout = inflater.inflate(R.layout.fragment_me, container, false);
        setUserIcon(layout.findViewById(R.id.user_profile_icon));

        AppCompatTextView nickname = layout.findViewById(R.id.profile_nickname);
        nickname.setText(profile.getUserName());

        layout.findViewById(R.id.user_profile_nickname_setting_layout).setOnClickListener(this);
        layout.findViewById(R.id.user_profile_icon_setting_layout).setOnClickListener(this);
        return layout;
    }

    private void setUserIcon(AppCompatImageView imageView) {
        Config.UserProfile profile = getContainer().config().getUserProfile();
        Drawable saved = profile.getProfileIcon();
        RoundedBitmapDrawable drawable =
                saved instanceof RoundedBitmapDrawable ? (RoundedBitmapDrawable) saved : null;

        if (drawable == null) {
            drawable = RoundedBitmapDrawableFactory.create(getResources(),
                    BitmapFactory.decodeResource(getResources(),
                    UserProfileUtil.getUserProfileIcon(profile.getUserId())));
            drawable.setCircular(true);
            profile.setProfileIcon(drawable);
        }

        imageView.setImageDrawable(drawable);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.user_profile_nickname_setting_layout:
                break;
            case R.id.user_profile_icon_setting_layout:
                break;
        }
    }
}
