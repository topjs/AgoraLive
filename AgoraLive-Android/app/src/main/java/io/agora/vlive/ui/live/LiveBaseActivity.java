package io.agora.vlive.ui.live;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import io.agora.vlive.R;
import io.agora.vlive.camera.CameraProxy;
import io.agora.vlive.ui.BaseActivity;

/**
 * Common capabilities of a live room. Such as, camera capture，
 * , agora rtc, and so on.
 */
public abstract class LiveBaseActivity extends BaseActivity {
    protected static final String[] PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private static final int PERMISSION_REQ = 1;

    private CameraProxy mCameraProxy = CameraProxy.create(application());

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkPermissions();
    }

    protected void checkPermissions() {
        if (!permissionArrayGranted()) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_REQ);
        } else {
            onPermissionGranted();
        }
    }

    private boolean permissionGranted(String permission) {
        return ContextCompat.checkSelfPermission(
                this, permission) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean permissionArrayGranted() {
        boolean granted = true;
        for (String per : PERMISSIONS) {
            if (!permissionGranted(per)) {
                granted = false;
                break;
            }
        }
        return granted;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQ) {
            if (permissionArrayGranted()) {
                onPermissionGranted();
            } else {
                Toast.makeText(this, R.string.permission_not_granted, Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    protected abstract void onPermissionGranted();

    protected CameraProxy cameraProxy() {
        return mCameraProxy;
    }
}
