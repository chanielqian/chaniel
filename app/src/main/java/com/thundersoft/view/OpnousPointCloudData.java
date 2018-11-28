package com.thundersoft.view;

import android.opengl.GLES20;
import android.util.Log;

import com.thundersoft.opnoustofdemo.TofCameraActivity;

import java.io.File;
import java.io.FileInputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/******************************************************************************
 ** File name: OpnousPointCloudData                                          **
 ** Creation date: 18-8-17                                                   **
 ** Author: Junxin Gao                                                       **
 ** Description:                                                             **
 **                                                                          **
 ******************************************************************************/
class OpnousPointCloudData implements PointCloudData {
    ByteBuffer mBuffer;
    boolean mError = false;
    private Lock mLock = new ReentrantLock();
    private String TAG = "OpnousPointCloudData";

    public OpnousPointCloudData(ByteBuffer src) {
        acquireLock();
        mBuffer = src;
        releaseLock();
    }

    @Override
    public Buffer getBuffer() {
        mBuffer.position(0);
        FloatBuffer ret = mBuffer.asFloatBuffer();
        ret.position(0);
        return ret;
    }

    @Override
    public boolean isValid() {
        return mError == false;
    }

    @Override
    public int getStride() {
        return 4 * 3;
    }

    @Override
    public int getPixelChannel() {
        return 3;
    }

    @Override
    public int getGLType() {
        return GLES20.GL_FLOAT;
    }

    @Override
    public int getElementNum() {
        return mBuffer.capacity() / getStride();
    }

    @Override
    public void acquireLock() {
        mLock.lock();
    }

    @Override
    public void releaseLock() {
        mLock.unlock();
    }

    @Override
    public void loadBuffer(ByteBuffer src) {
        acquireLock();
        mBuffer.position(0);
        mBuffer.put(src);
        releaseLock();
    }
}
