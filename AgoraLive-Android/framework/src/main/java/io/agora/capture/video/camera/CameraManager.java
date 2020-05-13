package io.agora.capture.video.camera;

import android.content.Context;
import android.view.SurfaceView;
import android.view.TextureView;

import io.agora.framework.modules.channels.ChannelManager;
import io.agora.framework.modules.consumers.SurfaceViewConsumer;
import io.agora.framework.modules.consumers.TextureViewConsumer;
import io.agora.framework.modules.processors.IPreprocessor;

/**
 * VideoManager is designed as the up-level encapsulation of
 * video module. It opens a series of APIs to the outside world,
 * and makes camera behavior much easier by containing some of
 * the camera logical procedures.
 * It can be seen as a particular utility class to control the
 * camera video channel, which is defined as one implementation
 * of the video channel designed in the framework.
 */
public class CameraManager {
    // CameraManager only controls camera channel
    private static final int CHANNEL_ID = ChannelManager.ChannelID.CAMERA;

    private static final int DEFAULT_FACING = Constant.CAMERA_FACING_FRONT;

    private VideoModule mVideoModule;
    private CameraVideoChannel mCameraChannel;

    /**
     * Initializes the camera video channel, loads all the
     * resources needed during camera capturing.
     * @param context Android context
     * @param preprocessor usually is the implementation
     *                     of a third-party beautification library
     * @param facing must be one of Constant.CAMERA_FACING_FRONT
     *               and Constant.CAMERA_FACING_BACK
     * @see io.agora.capture.video.camera.Constant
     */
    public CameraManager(Context context, IPreprocessor preprocessor, int facing) {
        init(context, preprocessor, facing);
    }

    public CameraManager(Context context, IPreprocessor preprocessor) {
        init(context, preprocessor, DEFAULT_FACING);
    }

    /**
     * Initializes the camera video channel, loads all the
     * resources needed during camera capturing.
     * @param context Android context
     * @param preprocessor usually is the implementation
     *                     of a third-party beautification library
     * @param facing must be one of Constant.CAMERA_FACING_FRONT
     *               and Constant.CAMERA_FACING_BACK
     * @see io.agora.capture.video.camera.Constant
     */
    private void init(Context context, IPreprocessor preprocessor, int facing) {
        mVideoModule = VideoModule.instance();
        if (!mVideoModule.hasInitialized()) {
            mVideoModule.init(context);
        }

        // The preprocessor must be set before
        // the video channel starts
        mVideoModule.setPreprocessor(CHANNEL_ID, preprocessor);
        mVideoModule.startChannel(CHANNEL_ID);
        mCameraChannel = (CameraVideoChannel)
                mVideoModule.getVideoChannel(CHANNEL_ID);
        mCameraChannel.setFacing(facing);
    }

    public void enablePreprocessor(boolean enabled) {
        if (mCameraChannel != null) {
            mCameraChannel.enablePreProcess(enabled);
        }
    }

    /**
     * Set camera preview. The view must be set before
     * attached to the window.
     * Currently only the latest preview set will display
     * local videos
     * If the TextureView is detached from the window,
     * it's previewing will be automatically stopped and it
     * is removed from the consumer list.
     * @param textureView
     */
    public void setPreview(TextureView textureView) {
        TextureViewConsumer consumer = new TextureViewConsumer();
        textureView.setSurfaceTextureListener(consumer);
    }

    /**
     * Set camera preview. The view must be set before
     * attached to the window.
     * Currently only the latest preview set will display
     * local videos
     * If the SurfaceView is detached from the window, it's
     * previewing will be automatically stopped and it
     * is removed from the consumer list.
     * @param surfaceView
     */
    public void setPreview(SurfaceView surfaceView) {
        surfaceView.getHolder().addCallback(
                new SurfaceViewConsumer(surfaceView));
    }

    public void setFacing(int facing) {
        if (mCameraChannel != null) {
            mCameraChannel.setFacing(facing);
        }
    }

    public void setPictureSize(int width, int height) {
        if (mCameraChannel != null) {
            mCameraChannel.setPictureSize(width, height);
        }
    }

    /**
     * Set the desired frame rate of the capture.
     * If not set, the default frame rate is 24
     * @param frameRate
     */
    public void setFrameRate(int frameRate) {
        if (mCameraChannel != null) {
            mCameraChannel.setIdealFrameRate(frameRate);
        }
    }

    public void startCapture() {
        if (mCameraChannel != null) {
            mCameraChannel.startCapture();
        }
    }

    public void stopCapture() {
        if (mCameraChannel != null) {
            mCameraChannel.stopCapture();
        }
    }

    public void switchCamera() {
        if (mCameraChannel != null) {
            mCameraChannel.switchCamera();
        }
    }
}
