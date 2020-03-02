package io.agora.vlive.camera;

import android.content.Context;
import android.view.TextureView;

import androidx.annotation.NonNull;

public class CameraProxy {
    private static CameraProxy sInstance;

    private Context mContext;
    private CameraCapture mCameraCapture;
    private CameraRender mCameraRender;

    public static CameraProxy create(Context context) {
        if (sInstance == null) {
            synchronized (CameraProxy.class) {
                if (sInstance == null) {
                    sInstance = new CameraProxy(context);
                }
            }
        }

        return sInstance;
    }

    private CameraProxy(Context context) {
        mContext = context;
        mCameraCapture = new CameraCapture();
        mCameraRender = new CameraRender();
    }

    public void setRenderView(@NonNull TextureView textureView) {
        mCameraRender.setRenderView(textureView);
    }
}
