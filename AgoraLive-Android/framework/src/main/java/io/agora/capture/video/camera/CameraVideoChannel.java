package io.agora.capture.video.camera;

import android.content.Context;

import io.agora.framework.modules.channels.ChannelManager;
import io.agora.framework.modules.channels.VideoChannel;

public class CameraVideoChannel extends VideoChannel {
    private static final String TAG = CameraVideoChannel.class.getSimpleName();

    private static final int WIDTH = 1920;
    private static final int HEIGHT = 1080;
    private static final int FRAME_RATE = 24;
    private static final int FACING = Constant.CAMERA_FACING_FRONT;

    private VideoCapture mVideoCapture;
    private volatile boolean mCapturedStarted;

    private int mWidth = WIDTH;
    private int mHeight = HEIGHT;
    private int mFrameRate = FRAME_RATE;
    private int mFacing = FACING;

    public CameraVideoChannel(Context context, int id) {
        super(context, id);
    }

    @Override
    protected void onChannelContextCreated() {
        mVideoCapture = VideoCaptureFactory.createVideoCapture(getChannelContext().getContext());
    }

    /**
     * Configuration of camera, must be called before the capture is started.
     * The width is usually larger than the height. The width, height and
     * frame rate that are actually chosen depend on the hardware capabilities.
     * @param width expected width of camera capture
     * @param height expected height of camera capture
     * @param frameRate expected capture frame rate
     * @param facing io.agora.capture.video.camera.Constant.CAMERA_FACING_FRONT or
     *               io.agora.capture.video.camera.Constant.CAMERA_FACING_BACK
     */
    public void config(int width, int height, int frameRate, int facing) {
        mWidth = width;
        mHeight = height;
        mFrameRate = frameRate;
        mFacing = facing;
    }

    public void startCapture() {
        if (isRunning() && !mCapturedStarted) {
            getHandler().post(() -> {
                mVideoCapture.connectChannel(ChannelManager.ChannelID.CAMERA);
                mVideoCapture.setSharedContext(getChannelContext().getEglCore().getEGLContext());
                mVideoCapture.allocate(mWidth, mHeight, mFrameRate, mFacing);
                mVideoCapture.startCaptureMaybeAsync(false);
                mCapturedStarted = true;
            });
        }
    }

    public void switchCamera() {
        if (isRunning() && mCapturedStarted) {
            getHandler().post(() -> {
                mVideoCapture.deallocate();
                switchCameraFacing();
                mVideoCapture.allocate(mWidth, mHeight, mFrameRate, mFacing);
                mVideoCapture.startCaptureMaybeAsync(false);
            });
        }
    }

    private void switchCameraFacing() {
        if (mFacing == Constant.CAMERA_FACING_FRONT) {
            mFacing = Constant.CAMERA_FACING_BACK;
        } else if (mFacing == Constant.CAMERA_FACING_BACK) {
            mFacing = Constant.CAMERA_FACING_FRONT;
        }
    }

    public void stopCapture() {
        if (isRunning() && mCapturedStarted) {
            getHandler().post(() -> {
                mVideoCapture.deallocate();
                mCapturedStarted = false;
            });
        }
    }

    public boolean hasCaptureStarted() {
        return mCapturedStarted;
    }
}
