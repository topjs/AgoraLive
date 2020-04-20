package io.agora.vlive.ui.components;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.io.IOException;

@TargetApi(26)
public class VoiceIndicateGifView extends View {
    private static final String VOICE_INDICATE_NAME = "voice.gif";

    private Movie mMovie;
    private boolean mStarted;
    private long mMovieStartTimeStamp;
    private long mMovieStopTimeStamp = -1;
    private float mScaleX;
    private float mScaleY;

    public VoiceIndicateGifView(Context context) {
        super(context);
        setDefaultImage();
    }

    public VoiceIndicateGifView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setDefaultImage();
    }

    private void setDefaultImage() {
        try {
            mMovie = Movie.decodeStream(getContext().getAssets().open(VOICE_INDICATE_NAME));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!mStarted) {
            mMovieStartTimeStamp = 0;
            return;
        }

        long now = System.currentTimeMillis();
        if (-1 < mMovieStopTimeStamp &&
                mMovieStopTimeStamp < now) {
            stop();
            return;
        }

        if (mMovieStartTimeStamp == 0) {
            mMovieStartTimeStamp = now;
        }

        if (mMovie != null) {
            if (mScaleX == 0 || mScaleY == 0) {
                int width = mMovie.width();
                int height = mMovie.height();
                int canvasW = getMeasuredWidth();
                int canvasH = getMeasuredHeight();
                mScaleX = canvasW / (float) width;
                mScaleY = canvasH / (float) height;
            }

            int duration = mMovie.duration();
            int time = (int) ((now - mMovieStartTimeStamp) % duration);
            mMovie.setTime(time);
            canvas.scale(mScaleX, mScaleY);
            mMovie.draw(canvas, 0, 0);
            invalidate();
        }
    }

    /**
     * Start gif animation repeatedly until stopped
     */
    public void start() {
        if (!mStarted) {
            mStarted = true;
            invalidate();
        }
    }

    /**
     * Start gif animation for a specific amount of time.
     * @param duration
     */
    public void start(long duration) {
        if (!mStarted) {
            mStarted = true;
            mMovieStopTimeStamp = System.currentTimeMillis() + duration;
            invalidate();
        }
    }

    public void stop() {
        if (mStarted) {
            mStarted = false;
            mMovieStartTimeStamp = 0;
            mMovieStopTimeStamp = -1;
            invalidate();
        }
    }

    public boolean hasStarted() {
        return mStarted;
    }
}
