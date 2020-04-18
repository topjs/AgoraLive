package io.agora.framework.consumers;

import android.opengl.EGL14;
import android.opengl.EGLSurface;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.util.Log;

import io.agora.framework.VideoCaptureFrame;
import io.agora.framework.VideoModule;
import io.agora.framework.channels.ChannelManager;
import io.agora.framework.channels.VideoChannel;
import io.agora.framework.gles.core.EglCore;
import io.agora.framework.gles.core.GlUtil;

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

    public BaseWindowConsumer(VideoModule videoModule) {
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
            // Before this step, no processing phase has ever changed
            // the image rotation, so we now need to take care of the
            // impact of camera orientation.
            int imageWidth = frame.mFormat.getWidth();
            int imageHeight = frame.mFormat.getHeight();

            if (frame.mRotation == 90 || frame.mRotation == 270) {
                imageWidth = frame.mFormat.getHeight();
                imageHeight = frame.mFormat.getWidth();
            }
            mMVPMatrix = GlUtil.changeMVPMatrix(
                    GlUtil.IDENTITY_MATRIX,
                    surfaceWidth, surfaceHeight,
                    imageWidth, imageHeight);
            mMVPInit = true;
        }

        if (frame.mFormat.getTexFormat() == GLES20.GL_TEXTURE_2D) {
            context.getProgram2D().drawFrame(
                    frame.mTextureId, frame.mTexMatrix, mMVPMatrix);
        } else if (frame.mFormat.getTexFormat() == GLES11Ext.GL_TEXTURE_EXTERNAL_OES) {
            context.getProgramOES().drawFrame(
                    frame.mTextureId, frame.mTexMatrix, mMVPMatrix);
        }

        if (drawingEglSurface != null) {
            eglCore.swapBuffers(drawingEglSurface);
        }
    }
}
