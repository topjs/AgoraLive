package io.agora.framework.channels;

import android.content.Context;
import android.opengl.EGLContext;
import android.opengl.EGLSurface;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import io.agora.framework.VideoCaptureFrame;
import io.agora.framework.camera.VideoCapture;
import io.agora.framework.consumers.IVideoConsumer;
import io.agora.framework.gles.ProgramTexture2d;
import io.agora.framework.gles.ProgramTextureOES;
import io.agora.framework.gles.core.EglCore;
import io.agora.framework.preprocess.IPreprocessor;
import io.agora.framework.producers.IVideoProducer;

public class VideoChannel extends HandlerThread {
    private static final String TAG = VideoChannel.class.getSimpleName();

    private int mChannelId;
    private boolean mOffScreenMode = true;

    private IVideoProducer mProducer;
    private List<IVideoConsumer> mOnScreenConsumers = new ArrayList<>();
    private List<IVideoConsumer> mOffScreenConsumers = new ArrayList<>();
    private IPreprocessor mPreprocessor;
    private Handler mHandler;

    private ChannelContext mContext;
    private EGLSurface mDummyEglSurface;

    VideoChannel(Context context, int id) {
        super(ChannelManager.ChannelID.toString(id));
        mChannelId = id;
        mContext = new ChannelContext();
        mContext.setContext(context);
    }

    void setPreprocessor(IPreprocessor preprocessor) {
        mPreprocessor = preprocessor;
    }

    @Override
    public void run() {
        init();
        super.run();
        release();
    }

    private void init() {
        Log.i(TAG, "channel opengl init");
        initOpenGL();
        initPreprocessor();
        onChannelContextCreated();
    }

    // The initialization phase for sub classes
    protected void onChannelContextCreated() {

    }

    private void initOpenGL() {
        EglCore eglCore = new EglCore();
        mContext.setEglCore(eglCore);
        mDummyEglSurface = eglCore.createOffscreenSurface(1, 1);
        eglCore.makeCurrent(mDummyEglSurface);
        mContext.setProgram2D(new ProgramTexture2d());
        mContext.setProgramOES(new ProgramTextureOES());
    }

    private void initPreprocessor() {
        if (mPreprocessor != null) {
            mPreprocessor.initPreprocessor();
        }
    }

    private void release() {
        Log.i(TAG, "channel opengl release");
        releasePreprocessor();
        releaseOpenGL();
    }

    private void releasePreprocessor() {
        if (mPreprocessor != null) {
            mPreprocessor.releasePreprocessor(getChannelContext());
            mPreprocessor = null;
        }
    }

    private void releaseOpenGL() {
        mContext.getProgram2D().release();
        mContext.getProgramOES().release();
        mContext.getEglCore().releaseSurface(mDummyEglSurface);
        mContext.getEglCore().release();
        mContext = null;
    }

    public ChannelContext getChannelContext() {
        return mContext;
    }

    IPreprocessor getPreprocessor() {
        return mPreprocessor;
    }

    void startChannel() {
        if (isRunning()) {
            return;
        }
        start();
        mHandler = new Handler(getLooper());
    }

    public Handler getHandler() {
        checkThreadRunningState();
        return mHandler;
    }

    void stopChannel() {
        Log.i(TAG, "StopChannel");
        if (mProducer != null) {
            mProducer.disconnect();
            mProducer = null;
        }

        if (!mOffScreenConsumers.isEmpty()) {
            for (IVideoConsumer consumer : mOffScreenConsumers) {
                consumer.disconnectChannel(mChannelId);
            }
        }
        mOffScreenConsumers.clear();

        removeOnScreenConsumer();
        quit();
    }

    private void resetOpenGLSurface() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                makeDummySurfaceCurrent();
            }
        });
    }

    private void removeOnScreenConsumer() {
        if (mOnScreenConsumers != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mOnScreenConsumers.clear();
                    // To remove on-screen consumer, we need
                    // to reset the GLSurface and maintain
                    // the OpenGL context properly.
                    makeDummySurfaceCurrent();
                }
            });
        }
    }

    public boolean isRunning() {
        return isAlive();
    }

    void connectProducer(IVideoProducer producer) {
        checkThreadRunningState();
        if (mProducer == null) {
            mProducer = producer;
        }
    }

    public void disconnectProducer() {
        checkThreadRunningState();
        mProducer = null;
    }

    void connectConsumer(final IVideoConsumer consumer, int type) {
        checkThreadRunningState();

        mHandler.post(() -> {
            if (type == IVideoConsumer.TYPE_ON_SCREEN) {
                if (!mOnScreenConsumers.contains(consumer)) {
                    Log.d(TAG, "On-screen consumer connected:" + consumer);
                    mOnScreenConsumers.add(consumer);
                }
            } else if (type == IVideoConsumer.TYPE_OFF_SCREEN) {
                if (!mOffScreenConsumers.contains(consumer)) {
                    Log.d(TAG, "Off-screen consumer connected:" + consumer);
                    mOffScreenConsumers.add(consumer);
                }
            }
        });
    }

    void disconnectConsumer(IVideoConsumer consumer) {
        checkThreadRunningState();

        mHandler.post(() -> {
            if (mOnScreenConsumers.contains(consumer)) {
                mOnScreenConsumers.remove(consumer);
                Log.d(TAG, "On-screen consumer disconnected:" + consumer);
                if (!mOffScreenMode && mOnScreenConsumers.size() == 0) {
                    mOffScreenConsumers.clear();
                }
            } else {
                mOffScreenConsumers.remove(consumer);
                Log.d(TAG, "Off-screen consumer disconnected:" + consumer);
                if (mOnScreenConsumers.isEmpty() &&
                        mOffScreenConsumers.isEmpty()) {
                    // If there's no consumer after remove
                    // this off screen consumer, the OpenGL
                    // drawing surface must be reset
                    resetOpenGLSurface();
                }
            }
        });
    }

    public void enablePreProcess(boolean enabled) {
        if (mPreprocessor != null) {
            mHandler.post(() -> mPreprocessor.enablePreProcess(enabled));
        }
    }

    public void pushVideoFrame(VideoCaptureFrame frame) {
        checkThreadRunningState();

        VideoCaptureFrame processFrame = frame;
        if (mPreprocessor != null) {
            processFrame = mPreprocessor.onPreProcessFrame(processFrame, getChannelContext());
            makeDummySurfaceCurrent();
        }

        if (mOnScreenConsumers != null && mOnScreenConsumers.size() > 0) {
            // By default we only render to the latest
            // registered on-screen consumer.
            mOnScreenConsumers.get(mOnScreenConsumers.size() - 1).onConsumeFrame(processFrame, mContext);
            makeDummySurfaceCurrent();
        }

        for (IVideoConsumer consumer : mOffScreenConsumers) {
            consumer.onConsumeFrame(processFrame, mContext);
            makeDummySurfaceCurrent();
        }
    }

    private void makeDummySurfaceCurrent() {
        // Every time after the preprocessor or consumers do
        // their jobs, we may need to restore the original
        // dummy EGL surface. Thus the current EGL context
        // will remain consistent even if the surfaces or
        // pixel buffers used by preprocessors or consumers
        // are destroyed in or out of the OpenGL threads.
        if (!mContext.isCurrent(mDummyEglSurface)) {
            mContext.makeCurrent(mDummyEglSurface);
        }
    }

    private void checkThreadRunningState() {
        if (!isAlive()) {
            throw new IllegalStateException("Video Channel is not alive");
        }
    }

    void enableOffscreenMode(boolean enabled) {
        mOffScreenMode = enabled;
    }

    public void setBlurValue(float blur) {
        if (mPreprocessor != null) {
            mPreprocessor.setBlurValue(blur);
        }
    }

    public void setWhitenValue(float whiten) {
        if (mPreprocessor != null) {
            mPreprocessor.setWhitenValue(whiten);
        }
    }

    public void setCheekValue(float cheek) {
        if (mPreprocessor != null) {
            mPreprocessor.setCheekValue(cheek);
        }
    }

    public void setEyeValue(float eye) {
        if (mPreprocessor != null) {
            mPreprocessor.setEyeValue(eye);
        }
    }

    public static class ChannelContext {
        private Context mContext;
        private EglCore mEglCore;
        private ProgramTexture2d mProgram2D;
        private ProgramTextureOES mProgramOES;

        public Context getContext() {
            return mContext;
        }

        public void setContext(Context context) {
            this.mContext = context;
        }

        public EglCore getEglCore() {
            return mEglCore;
        }

        private void setEglCore(EglCore mEglCore) {
            this.mEglCore = mEglCore;
        }

        public EGLContext getEglContext() {
            return getEglCore().getEGLContext();
        }

        public ProgramTexture2d getProgram2D() {
            return mProgram2D;
        }

        private void setProgram2D(ProgramTexture2d mFullFrameRectTexture2D) {
            this.mProgram2D = mFullFrameRectTexture2D;
        }

        public ProgramTextureOES getProgramOES() {
            return mProgramOES;
        }

        private void setProgramOES(ProgramTextureOES mTextureOES) {
            this.mProgramOES = mTextureOES;
        }

        public EGLSurface getCurrentSurface() {
            return mEglCore.getCurrentDrawingSurface();
        }

        public void makeCurrent(EGLSurface surface) {
            mEglCore.makeCurrent(surface);
        }

        public boolean isCurrent(EGLSurface surface) {
            return mEglCore.isCurrent(surface);
        }
    }
}
