package io.agora.vlive.ui.main;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.faceunity.FURenderer;
import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.lang.reflect.Field;

import io.agora.framework.PreprocessorFaceUnity;
import io.agora.framework.VideoModule;
import io.agora.framework.channels.ChannelManager;
import io.agora.rtm.ErrorInfo;
import io.agora.rtm.ResultCallback;
import io.agora.vlive.Config;
import io.agora.vlive.R;
import io.agora.vlive.proxy.struts.request.Request;
import io.agora.vlive.proxy.struts.request.UserRequest;
import io.agora.vlive.proxy.struts.response.AppVersionResponse;
import io.agora.vlive.proxy.struts.response.CreateUserResponse;
import io.agora.vlive.proxy.struts.response.GiftListResponse;
import io.agora.vlive.proxy.struts.response.LoginResponse;
import io.agora.vlive.proxy.struts.response.MusicListResponse;
import io.agora.vlive.proxy.struts.response.Response;
import io.agora.vlive.ui.BaseActivity;
import io.agora.vlive.utils.Global;

public class MainActivity extends BaseActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private RelativeLayout mContainerLayout;
    private BottomNavigationView mNavView;
    private NavController mNavController;
    private int mTopMargin;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideStatusBar(true);
        setContentView(R.layout.activity_main);
        initUI();
        initAsync();
    }

    private void initUI() {
        initNavigation();
    }

    private void initNavigation() {
        mNavView = findViewById(R.id.nav_view);
        changeItemHeight(mNavView);
        mNavView.setItemIconTintList(null);
        mNavController = Navigation.findNavController(this, R.id.nav_host_fragment);

        mNavView.setOnNavigationItemSelectedListener(item -> {
                int selectedId = item.getItemId();
                int currentId = mNavController.getCurrentDestination() == null ?
                        0 : mNavController.getCurrentDestination().getId();

                // Do not respond to this click event because
                // we do not want to refresh this fragment
                // by repeatedly selecting the same menu item.
                if (selectedId == currentId) return false;

                NavigationUI.onNavDestinationSelected(item, mNavController);
                hideStatusBar(getWindow(), true);

                // Profile fragment needs to be drawn
                // to the top of the screen.
                int top = selectedId == R.id.navigation_me ? 0 : mTopMargin;
                mContainerLayout.setPadding(0, top, 0, 0);
                return true;
            }
        );
    }

    public void setNavigationSelected(int resId, Bundle bundle) {
        mNavView.setSelectedItemId(resId);
        mNavController.navigate(resId, bundle);
    }

    @Override
    protected void onGlobalLayoutCompleted() {
        mContainerLayout = findViewById(R.id.main_activity_container);
        mTopMargin = mContainerLayout.getPaddingTop();
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

    private void initAsync() {
        new Thread(() -> {
            login();
            getGiftList();
            getMusicList();
            checkUpdate();
        }).start();
    }

    private void checkUpdate() {
        if (!config().hasCheckedVersion()) {
            sendRequest(Request.APP_VERSION, getAppVersion());
        }
    }

    private String getAppVersion() {
        try {
            return getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void onAppVersionResponse(AppVersionResponse response) {
        config().setVersionInfo(response.data);
    }

    private void login() {
        Config.UserProfile profile = config().getUserProfile();
        initUserFromStorage(profile);
        if (!profile.isValid()) {
            createUser();
        } else {
            loginToServer();
        }
    }

    private void initUserFromStorage(Config.UserProfile profile) {
        profile.setUserId(preferences().getString(Global.Constants.KEY_PROFILE_UID, null));
        profile.setUserName(preferences().getString(Global.Constants.KEY_USER_NAME, null));
        profile.setImageUrl(preferences().getString(Global.Constants.KEY_IMAGE_URL, null));
        profile.setToken(preferences().getString(Global.Constants.KEY_TOKEN, null));
    }

    private void createUser() {
        sendRequest(Request.CREATE_USER, new UserRequest());
    }

    @Override
    public void onCreateUserResponse(CreateUserResponse response) {
        createUserFromResponse(response);
        loginToServer();
    }

    private void createUserFromResponse(CreateUserResponse response) {
        Config.UserProfile profile = config().getUserProfile();
        profile.setUserId(response.data.userId);
        preferences().edit().putString(Global.Constants.KEY_PROFILE_UID, profile.getUserId()).apply();

    }

    private void loginToServer() {
        sendRequest(Request.USER_LOGIN, config().getUserProfile().getUserId());
    }

    @Override
    public void onLoginResponse(LoginResponse response) {
        if (response != null && response.code == Response.SUCCESS) {
            config().getUserProfile().setToken(response.data.userToken);
            config().getUserProfile().setRtmToken(response.data.rtmToken);
            preferences().edit().putString(Global.Constants.KEY_TOKEN, response.data.userToken).apply();
            joinRtmServer();
        }
    }

    private void joinRtmServer() {
        rtmClient().login(config().getUserProfile().getRtmToken(),
                config().getUserProfile().getUserId(), new ResultCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

            }

            @Override
            public void onFailure(ErrorInfo errorInfo) {

            }
        });
    }

    private void getGiftList() {
        sendRequest(Request.GIFT_LIST, null);
    }

    @Override
    public void onGiftListResponse(GiftListResponse response) {
        config().setGiftList(response.data);
        Log.i(TAG, "onGiftListFinished");
    }

    private void getMusicList() {
        sendRequest(Request.MUSIC_LIST, null);
    }

    @Override
    public void onMusicLisResponse(MusicListResponse response) {
        config().setMusicList(response.data);
        Log.i(TAG, "onMusicListFinished");
    }

    @Override
    public void onResponseError(int requestType, int error, String message) {
        Log.e(TAG, "request:" + requestType + " error:" + error + " msg:" + message);
    }
}
