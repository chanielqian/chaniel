package com.thundersoft.view;

import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/******************************************************************************
 ** File name: TofView                                                       **
 ** Creation date: 18-8-12                                                   **
 ** Author: Junxin Gao                                                       **
 ** Description:                                                             **
 **           Copyright (c) 2018 ThunderSoft All rights reserved.            **
 ******************************************************************************/
public class GLPerspective {

    private GLPerspective() {
    }

    static public void setScaleXY(float scaleX, float scaleY) {
        mScaleX = scaleX;
        mScaleY = scaleY;
    }

    static public void adjustScaleFactor(float factor) {
        mScaleFactor *= factor;
    }

    static public void rotate(float rotateX, float rotateY) {
        mRotateX += rotateX;
        mRotateY += rotateY;
    }

    static public void resetViewPort() {
        mRotateX = 0f;
        mRotateY = 0f;
        mScaleFactor = 1.0f;
    }

    static public void setPreferredScale(float x, float y, float z) {
        mObjPreferredXScale = x;
        mObjPreferredYScale = y;
        mObjPreferredZScale = z;
    }

    static public FloatBuffer calculateMatrix() {
        float[] matrix = new float[16];
        float[] lookatM = new float[16];
        float[] resultM = new float[16];

        Matrix.setIdentityM(matrix, 0);
        Matrix.scaleM(matrix, 0,
                mObjPreferredXScale * mScaleFactor * mScaleX,
                mObjPreferredYScale * mScaleFactor * mScaleY,
                mObjPreferredZScale * mScaleFactor);

        Matrix.rotateM(matrix, 0, mRotateX, 0, 1.0f, 0);
        Matrix.rotateM(matrix, 0, mRotateY, 1.0f, 0, 0);

        Matrix.setLookAtM(lookatM, 0, 0, 1.0f, 0f, 0f, 0f, 0f, 0, 0f, 1.0f);

        Matrix.multiplyMM(resultM, 0, lookatM, 0, matrix, 0);

        if (null == mMatrixBuffer) {
            mMatrixBuffer = ByteBuffer.allocateDirect(16 * 4).order(ByteOrder.nativeOrder());
        }
        FloatBuffer result = mMatrixBuffer.asFloatBuffer();
        result.position(0);
        result.put(resultM);
        result.position(0);

        return result;
    }

    static private float mObjPreferredXScale = 1.0f;
    static private float mObjPreferredYScale = 1.0f;
    static private float mObjPreferredZScale = 1.0f;


    static private float mScaleFactor = 1.0f;
    static private float mScaleX = 1.0f;
    static private float mScaleY = 1.0f;

    static private float mRotateX = 0;
    static private float mRotateY = 0;

    static private ByteBuffer mMatrixBuffer;
}
