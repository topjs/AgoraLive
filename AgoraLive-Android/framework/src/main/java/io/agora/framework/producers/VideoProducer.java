package io.agora.framework.producers;

import android.opengl.GLES11Ext;
import android.os.Handler;
import android.util.Log;

import io.agora.framework.VideoCaptureFrame;
import io.agora.framework.VideoModule;
import io.agora.framework.channels.VideoChannel;

public abstract class VideoProducer implements IVideoProducer {
    private static final String TAG = VideoProducer.class.getSimpleName();

    private VideoChannel videoChannel;
    protected volatile Handler pChannelHandler;

    @Override
    public void connectChannel(int channelId) {
        videoChannel = VideoModule.instance().connectProducer(this, channelId);
        pChannelHandler = videoChannel.getHandler();
    }

    @Override
    public void pushVideoFrame(final VideoCaptureFrame frame) {
        if (pChannelHandler == null) {
            return;
        }

        pChannelHandler.post(() -> {
            // Since the captured image format is reused by capture
            // module, we need to reset the texture format as
            // a workaround.
            // TODO Need config the capture texture format
            frame.mFormat.setTexFormat(GLES11Ext.GL_TEXTURE_EXTERNAL_OES);

            final VideoCaptureFrame out = new VideoCaptureFrame(frame);
            try {
                frame.mSurfaceTexture.updateTexImage();
                frame.mSurfaceTexture.getTransformMatrix(out.mTexMatrix);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }

            if (videoChannel != null) {
                videoChannel.pushVideoFrame(out);
            }
        });
    }

    @Override
    public void disconnect() {
        Log.i(TAG, "disconnect");

        if (videoChannel != null) {
            videoChannel.disconnectProducer();
            videoChannel = null;
        }
    }
}
