package io.agora.framework;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;

import com.faceunity.FURenderer;

import io.agora.framework.channels.VideoChannel;
import io.agora.framework.preprocess.IPreprocessor;

public class PreprocessorFaceUnity implements IPreprocessor {
    private final static String TAG = PreprocessorFaceUnity.class.getSimpleName();

    private FURenderer mFURenderer;
    private Context mContext;
    private boolean mEnabled;

    public PreprocessorFaceUnity(Context context) {
        mContext = context;
        mEnabled = true;
    }

    @Override
    public void onPreProcessFrame(VideoCaptureFrame outFrame, VideoChannel.ChannelContext context) {
        if (mFURenderer == null || !mEnabled) {
            return;
        }

        outFrame.mTextureId = mFURenderer.onDrawFrame(outFrame.mImage,
                outFrame.mTextureId, outFrame.mFormat.getWidth(),
                outFrame.mFormat.getHeight());

        // The texture is transformed to texture2D by beauty module.
        outFrame.mFormat.setTexFormat(GLES20.GL_TEXTURE_2D);
    }

    @Override
    public void initPreprocessor() {
        mFURenderer = new FURenderer.Builder(mContext).
                inputImageFormat(FURenderer.FU_ADM_FLAG_EXTERNAL_OES_TEXTURE).build();
        mFURenderer.onSurfaceCreated();
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
}
