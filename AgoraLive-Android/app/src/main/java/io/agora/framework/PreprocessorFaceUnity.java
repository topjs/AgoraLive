package io.agora.framework;

import android.content.Context;
import android.opengl.GLES20;

import com.faceunity.FURenderer;
import com.faceunity.entity.Effect;

import io.agora.capture.video.camera.VideoCaptureFrame;
import io.agora.framework.modules.channels.VideoChannel;
import io.agora.framework.modules.processors.IPreprocessor;

public class PreprocessorFaceUnity implements IPreprocessor {
    private final static String TAG = PreprocessorFaceUnity.class.getSimpleName();
    private final static int ANIMOJI_COUNT = 2;

    public static final float DEFAULT_BLUR_VALUE = 0.7f;
    public static final float DEFAULT_WHITEN_VALUE = 0.3f;
    public static final float DEFAULT_CHEEK_VALUE = 0f;
    public static final float DEFAULT_EYE_VALUE = 0.4f;

    private FURenderer mFURenderer;
    private Context mContext;
    private boolean mEnabled;
    private Effect mEffectNone;
    private Effect mEffectBackground;
    private Effect[] mAnimojiEffects;

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
                inputImageFormat(FURenderer.FU_ADM_FLAG_EXTERNAL_OES_TEXTURE)
                .setNeedAnimoji3D(true).build();
        mFURenderer.onSurfaceCreated();
        mFURenderer.onBlurLevelSelected(DEFAULT_BLUR_VALUE);
        mFURenderer.onColorLevelSelected(DEFAULT_WHITEN_VALUE);
        mFURenderer.onCheekVSelected(DEFAULT_CHEEK_VALUE);
        mFURenderer.onEyeEnlargeSelected(DEFAULT_EYE_VALUE);
        initAnimoji();
    }

    private void initAnimoji() {
        mAnimojiEffects = new Effect[ANIMOJI_COUNT];
        mAnimojiEffects[0] = new Effect("hashiqi_Animoji",
                -1, "hashiqi_Animoji.bundle", 4,
                Effect.EFFECT_TYPE_ANIMOJI, 0);
        mAnimojiEffects[1] = new Effect("qgirl_Animoji",
                -1, "qgirl.bundle", 4,
                Effect.EFFECT_TYPE_ANIMOJI, 0);
        mEffectNone = new Effect("none", -1,
                "none", 1, Effect.EFFECT_TYPE_NONE, 0);
        mEffectBackground = new Effect("white_bg.bundle",
                -1, "white_bg.bundle", 1,
                Effect.EFFECT_TYPE_ANIMOJI, 0);
    }

    public void onAnimojiSelected(int index) {
        if (mFURenderer != null) {
            if (0 <= index && index < ANIMOJI_COUNT) {
                mFURenderer.onVirtualImageSelected(
                        mAnimojiEffects[index], mEffectBackground);
            } else {
                mFURenderer.onVirtualImageUnselected();
            }
        }
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
