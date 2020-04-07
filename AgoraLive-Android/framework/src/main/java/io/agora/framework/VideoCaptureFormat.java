// Copyright 2015 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package io.agora.framework;

public class VideoCaptureFormat {
    protected int mWidth;
    protected int mHeight;
    protected int mFrameRate;
    protected int mPixelFormat;
    protected int mTexFormat;

    public VideoCaptureFormat(int width, int height, int framerate, int pixelFormat, int texFormat) {
        mWidth = width;
        mHeight = height;
        mFrameRate = framerate;
        mPixelFormat = pixelFormat;
        mTexFormat = texFormat;
    }

    public int getWidth() {
        return mWidth;
    }

    public void setWidth(int width) {
        mWidth = width;
    }

    public int getHeight() {
        return mHeight;
    }

    public void setHeight(int height) {
        mHeight = height;
    }

    public int getFramerate() {
        return mFrameRate;
    }

    public int getPixelFormat() {
        return mPixelFormat;
    }

    public void setPixelFormat(int format) {
        mPixelFormat = format;
    }

    public int getTexFormat() {
        return mTexFormat;
    }

    public void setTexFormat(int format) {
        mTexFormat = format;
    }

    public String toString() {
        return "VideoCaptureFormat{" +
                "mFormat=" + mPixelFormat +
                "mFrameRate=" + mFrameRate +
                ", mWidth=" + mWidth +
                ", mHeight=" + mHeight +
                '}';
    }
}
