package com.thundersoft.view;

import java.nio.Buffer;
import java.nio.ByteBuffer;

/******************************************************************************
 ** File name: TofView                                                       **
 ** Creation date: 18-8-12                                                   **
 ** Author: Junxin Gao                                                       **
 ** Description:                                                             **
 **           Copyright (c) 2018 ThunderSoft All rights reserved.            **
 ******************************************************************************/
interface PointCloudData {
    boolean isValid();

    int getStride();

    int getPixelChannel();

    int getGLType();

    int getElementNum();

    void acquireLock();

    void releaseLock();

    void loadBuffer(ByteBuffer src);

    Buffer getBuffer();
}

