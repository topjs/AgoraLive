package io.agora.vlive.ui.main;

import android.os.Bundle;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import io.agora.vlive.R;
import io.agora.vlive.ui.BaseActivity;

public class MainActivity extends BaseActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideStatusBar(true);
        setContentView(R.layout.activity_main);
        initUI();
    }

    private void initUI() {
        initNavigation();
    }

    private void initNavigation() {
        BottomNavigationView navView = findViewById(R.id.nav_view);
        navView.setItemIconTintList(null);
        NavigationUI.setupWithNavController(navView,
        Navigation.findNavController(this, R.id.nav_host_fragment));
    }
}
