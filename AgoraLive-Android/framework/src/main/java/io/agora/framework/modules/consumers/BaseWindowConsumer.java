package io.agora.framework.modules.consumers;

import android.content.Context;
import android.opengl.EGL14;
import android.opengl.EGLSurface;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.view.Surface;
import android.view.WindowManager;

import io.agora.capture.video.camera.VideoCaptureFrame;
import io.agora.capture.video.camera.VideoModule;
import io.agora.framework.modules.channels.ChannelManager;
import io.agora.framework.modules.channels.VideoChannel;
import io.agora.framework.helpers.gles.core.EglCore;
import io.agora.framework.helpers.gles.core.GlUtil;

public abstract class BaseWindowConsumer implements IVideoConsumer {
    static final int CHANNEL_ID = ChannelManager.ChannelID.CAMERA;
    public static boolean DEBUG = false;

    VideoModule videoModule;
    VideoChannel videoChannel;

    private EGLSurface drawingEglSurface;
    volatile boolean needResetSurface = true;
    volatile boolean surfaceDestroyed;
    private float[] mMVPMatrix = new float[16];
    private boolean mMVPInit;

    // Refers to the rotation of current Activity
    // window obtained from the Display
    int pSurfaceRotation;

    int getSurfaceRotation(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        int rotation = wm != null ? wm.getDefaultDisplay().getRotation() : Surface.ROTATION_0;

        switch (rotation) {
            case Surface.ROTATION_90: return 90;
            case Surface.ROTATION_180: return 180;
            case Surface.ROTATION_270: return 270;
            case Surface.ROTATION_0:
            default: return 0;
        }
    }

    BaseWindowConsumer(VideoModule videoModule) {
        this.videoModule = videoModule;
    }

    @Override
    public void connectChannel(int channelId) {
        videoChannel = videoModule.connectConsumer(this, channelId, IVideoConsumer.TYPE_ON_SCREEN);
    }

    @Override
    public void disconnectChannel(int channelId) {
        videoModule.disconnectConsumer(this, channelId);
    }

    @Override
    public void onConsumeFrame(VideoCaptureFrame frame, VideoChannel.ChannelContext context) {
        drawFrame(frame, context);
    }

    private void drawFrame(VideoCaptureFrame frame, VideoChannel.ChannelContext context) {
        if (surfaceDestroyed) {
            return;
        }

        EglCore eglCore = context.getEglCore();
        if (needResetSurface) {
            if (drawingEglSurface != null && drawingEglSurface != EGL14.EGL_NO_SURFACE) {
                eglCore.releaseSurface(drawingEglSurface);
                eglCore.makeNothingCurrent();
                drawingEglSurface = null;
            }

            Object surface = onGetDrawingTarget();
            if (surface != null) {
                drawingEglSurface = eglCore.createWindowSurface(onGetDrawingTarget());
                needResetSurface = false;
            }
        }

        if (drawingEglSurface != null && !eglCore.isCurrent(drawingEglSurface)) {
            eglCore.makeCurrent(drawingEglSurface);
        }

        int surfaceWidth = onMeasuredWidth();
        int surfaceHeight = onMeasuredHeight();
        GLES20.glViewport(0, 0, surfaceWidth, surfaceHeight);

        if (!mMVPInit) {
            mMVPMatrix = GlUtil.changeMVPMatrix(
                    GlUtil.IDENTITY_MATRIX,
                    surfaceWidth, surfaceHeight,
                    frame.format.getWidth(),
                    frame.format.getHeight());
            mMVPInit = true;
        }

        if (frame.format.getTexFormat() == GLES20.GL_TEXTURE_2D) {
            context.getProgram2D().drawFrame(
                    frame.textureId, frame.textureTransform, mMVPMatrix);
        } else if (frame.format.getTexFormat() == GLES11Ext.GL_TEXTURE_EXTERNAL_OES) {
            context.getProgramOES().drawFrame(
                    frame.textureId, frame.textureTransform, mMVPMatrix);
        }

        if (drawingEglSurface != null) {
            eglCore.swapBuffers(drawingEglSurface);
        }
    }
}
