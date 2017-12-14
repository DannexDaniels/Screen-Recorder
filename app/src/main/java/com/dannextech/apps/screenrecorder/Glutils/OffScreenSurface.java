package com.dannextech.apps.screenrecorder.Glutils;

/**
 * Created by amoh on 12/11/2017.
 */

public class OffScreenSurface extends EglSurfaceBase {
    public OffScreenSurface(final EglCore eglBase, final int width, final int height) {
        super(eglBase);
        createOffscreenSurface(width, height);
        makeCurrent();
    }

    public void release() {
        releaseEglSurface();
    }

}

