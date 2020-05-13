package io.agora.framework;

import android.content.Context;
import android.opengl.GLES20;

import com.faceunity.FURenderer;

import io.agora.capture.video.camera.VideoCaptureFrame;
import io.agora.framework.modules.channels.VideoChannel;
import io.agora.framework.modules.processors.IPreprocessor;

public class PreprocessorFaceUnity implements IPreprocessor {
    private final static String TAG = PreprocessorFaceUnity.class.getSimpleName();

    public static final float DEFAULT_BLUR_VALUE = 0.7f;
    public static final float DEFAULT_WHITEN_VALUE = 0.3f;
    public static final float DEFAULT_CHEEK_VALUE = 0f;
    public static final float DEFAULT_EYE_VALUE = 0.4f;

    private FURenderer mFURenderer;
    private Context mContext;
    private boolean mEnabled;

    public PreprocessorFaceUnity(Context context) {
        mContext = context;
        mEnabled = true;
    }

    @Override
    public VideoCaptureFrame onPreProcessFrame(VideoCaptureFrame outFrame, VideoChannel.ChannelContext context) {
        if (mFURenderer == null || !mEnabled) {
            return outFrame;
        }

        outFrame.textureId = mFURenderer.onDrawFrame(outFrame.image,
                outFrame.textureId, outFrame.format.getWidth(),
                outFrame.format.getHeight());

        // The texture is transformed to texture2D by beauty module.
        outFrame.format.setTexFormat(GLES20.GL_TEXTURE_2D);
        return outFrame;
    }

    @Override
    public void initPreprocessor() {
        mFURenderer = new FURenderer.Builder(mContext).
                inputImageFormat(FURenderer.FU_ADM_FLAG_EXTERNAL_OES_TEXTURE).build();
        mFURenderer.onSurfaceCreated();
        mFURenderer.onBlurLevelSelected(DEFAULT_BLUR_VALUE);
        mFURenderer.onColorLevelSelected(DEFAULT_WHITEN_VALUE);
        mFURenderer.onCheekVSelected(DEFAULT_CHEEK_VALUE);
        mFURenderer.onEyeEnlargeSelected(DEFAULT_EYE_VALUE);
    }

    @Override
    public void enablePreProcess(boolean enabled) {
        mEnabled = enabled;
    }

    @Override
    public void releasePreprocessor(VideoChannel.ChannelContext context) {
        if (mFURenderer != null) {
            mFURenderer.onSurfaceDestroyed();
        }
    }

    @Override
    public void setBlurValue(float blur) {
        if (mFURenderer != null) {
            mFURenderer.onBlurLevelSelected(blur);
        }
    }

    @Override
    public void setWhitenValue(float whiten) {
        if (mFURenderer != null) {
            mFURenderer.onColorLevelSelected(whiten);
        }
    }

    @Override
    public void setCheekValue(float cheek) {
        if (mFURenderer != null) {
            mFURenderer.onCheekThinningSelected(cheek);
        }
    }

    @Override
    public void setEyeValue(float eye) {
        if (mFURenderer != null) {
            mFURenderer.onEyeEnlargeSelected(eye);
        }
    }
}
