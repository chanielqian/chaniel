package com.thundersoft.view;


import android.opengl.GLES20;
import android.util.Log;
/******************************************************************************
 ** File name: TofView                                                       **
 ** Creation date: 18-8-12                                                   **
 ** Author: Junxin Gao                                                       **
 ** Description:                                                             **
 **           Copyright (c) 2018 ThunderSoft All rights reserved.            **
 ******************************************************************************/
public class GLPointCloud {

    private static final String TAG = "GLPointCloud";

    private final String vertexShaderCode =

            "attribute vec4 vPosition;" +
                    "uniform mat4 uMat;" +
                    "void main() {" +
                    "vec4 wp = vPosition;" +
                    "  wp = uMat * wp;" +
                    "  gl_Position = wp;" +
                    "  gl_PointSize = 1.0;" +
                    "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "void main() {" +
                    "  gl_FragColor = vColor;" +
                    "}";

    private final int mProgram;

    float color[] = {0.63671875f, 0.76953125f, 0.22265625f, 0.0f};

    public GLPointCloud() {
        int vertexShader = loadShader(
                GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = loadShader(
                GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        mProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mProgram, vertexShader);
        GLES20.glAttachShader(mProgram, fragmentShader);
        GLES20.glLinkProgram(mProgram);
    }


    public void draw() {
        PointCloudData mCloudData = PointCloudController.getInstance().getPointCloudData();

        if (mCloudData == null || mCloudData.isValid() == false) {
            Log.e(TAG, "PointCloudData invalid");
            return;
        }

        GLES20.glUseProgram(mProgram);


        checkGlError("glUseProgram");

        int positionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");

        checkGlError("glGetAttribLocztion");

        GLES20.glEnableVertexAttribArray(positionHandle);

        checkGlError("glEnableVertexAttribArray");

        mCloudData.acquireLock();
        GLES20.glVertexAttribPointer(
                positionHandle, mCloudData.getPixelChannel(),
                mCloudData.getGLType(), false,
                mCloudData.getStride(), mCloudData.getBuffer());


        checkGlError("glVertexAttribPointer");


        int colorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");

        checkGlError("glGetUniformLocation");

        GLES20.glUniform4fv(colorHandle, 1, color, 0);

        checkGlError("glUniform4fv");

        int matHandle = GLES20.glGetUniformLocation(mProgram, "uMat");
        GLES20.glUniformMatrix4fv(matHandle, 1, false, GLPerspective.calculateMatrix());

        checkGlError("glUniformMatrix4fv");

        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, mCloudData.getElementNum());

        checkGlError("glDrawArrays");
        mCloudData.releaseLock();
        GLES20.glDisableVertexAttribArray(positionHandle);
    }


    public static int loadShader(int type, String shaderCode) {

        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }

    public static void checkGlError(String glOperation) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, glOperation + ": glError " + error);
            throw new RuntimeException(glOperation + ": glError " + error);
        }
    }
}
