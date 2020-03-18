package io.agora.vlive.camera;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.Log;
import android.view.TextureView;

import java.io.IOException;

public class CameraRender implements TextureView.SurfaceTextureListener {
    private static final String TAG = CameraRender.class.getSimpleName();

    private Camera mCamera;

    void setRenderView(TextureView textureView) {
        textureView.setSurfaceTextureListener(this);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
        Log.i(TAG, "onSurfaceTextureAvailable: width=" + width + " height=" + height + " " + surfaceTexture.toString());
        mCamera = Camera.open();
        try {
            surfaceTexture.setDefaultBufferSize(width, height);
            mCamera.setDisplayOrientation(90);
            mCamera.setPreviewTexture(surfaceTexture);
        } catch (IOException e) {
            e.printStackTrace();
        }

        mCamera.startPreview();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        Log.i(TAG, "onSurfaceTextureDestroyed:" + surfaceTexture.toString());
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

    }
}
