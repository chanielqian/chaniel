package com.thundersoft.view;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/******************************************************************************
 ** File name: TofView                                                       **
 ** Creation date: 18-8-12                                                   **
 ** Author: Junxin Gao                                                       **
 ** Description:                                                             **
 **           Copyright (c) 2018 ThunderSoft All rights reserved.            **
 ******************************************************************************/
public class TofRender implements GLSurfaceView.Renderer {

    private GLPointCloud mPointCloud = null;
    private boolean isPCDraw = false;

    public void setPCDraw(boolean PCDraw) {
        isPCDraw = PCDraw;
    }

    @Override

    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        if (mPointCloud == null) {
            mPointCloud = new GLPointCloud();
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        if (width > height) {
            GLPerspective.setScaleXY((float) height / (float) width, 1.0f);
        } else {
            GLPerspective.setScaleXY(1.0f, (float) width / (float) height);
        }
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        if (isPCDraw)
            mPointCloud.draw();
    }

    public void resetViewPort() {
        GLPerspective.resetViewPort();
    }

    public void scale(float factor) {
        GLPerspective.adjustScaleFactor(factor);
    }

    public void rotate(float x, float y) {
        GLPerspective.rotate(x, y);
    }
}
