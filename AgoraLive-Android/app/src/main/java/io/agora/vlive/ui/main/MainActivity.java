package io.agora.vlive.ui.main;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.lang.reflect.Field;

import io.agora.vlive.Config;
import io.agora.vlive.R;
import io.agora.vlive.proxy.struts.request.Request;
import io.agora.vlive.proxy.struts.request.UserRequest;
import io.agora.vlive.proxy.struts.response.CreateUserResponse;
import io.agora.vlive.ui.BaseActivity;
import io.agora.vlive.utils.Global;

public class MainActivity extends BaseActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideStatusBar(true);
        setContentView(R.layout.activity_main);
        initUI();
        initUser();
    }

    private void initUI() {
        initNavigation();
    }

    private void initNavigation() {
        BottomNavigationView navView = findViewById(R.id.nav_view);
        changeItemHeight(navView);
        navView.setItemIconTintList(null);
        NavigationUI.setupWithNavController(navView,
        Navigation.findNavController(this, R.id.nav_host_fragment));
    }

    private void changeItemHeight(@NonNull BottomNavigationView navView) {
        // Bottom navigation menu uses a hardcode menu item
        // height which cannot be changed by a layout attribute.
        // Change the item height using reflection for
        // a comfortable padding between icon and label.
        int itemHeight = getResources().getDimensionPixelSize(R.dimen.nav_bar_height);
        BottomNavigationMenuView menu =
                (BottomNavigationMenuView) navView.getChildAt(0);
        try {
            Field itemHeightField = BottomNavigationMenuView.class.getDeclaredField("itemHeight");
            itemHeightField.setAccessible(true);
            itemHeightField.set(menu, itemHeight);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private void initUser() {
        Config.UserProfile profile = config().getUserProfile();
        initUserFromStorage(profile);
        if (!profile.isValid()) {
            createUser();
        }
    }

    private void initUserFromStorage(Config.UserProfile profile) {
        profile.userId = preferences().getString(Global.Constants.KEY_PROFILE_UID, null);
        profile.userName = preferences().getString(Global.Constants.KEY_USER_NAME, null);
        profile.imageUrl = preferences().getString(Global.Constants.KEY_IMAGE_URL, null);
        profile.token = preferences().getString(Global.Constants.KEY_TOKEN, null);
    }

    private void createUser() {
        proxy().sendReq(Request.CREATE_USER, new UserRequest(), this);
    }

    @Override
    public void onCreateUserResponse(CreateUserResponse response) {
        createUserFromResponse(response);
    }

    private void createUserFromResponse(CreateUserResponse response) {
        Config.UserProfile profile = config().getUserProfile();
        profile.userId = response.data.userId;
        profile.token = response.data.token;

        preferences().edit().putString(Global.Constants.KEY_PROFILE_UID, profile.userId).apply();
        preferences().edit().putString(Global.Constants.KEY_TOKEN, profile.token).apply();
    }
}
