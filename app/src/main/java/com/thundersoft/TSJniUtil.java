package com.thundersoft;

import java.nio.ByteBuffer;

import android.graphics.Bitmap;
import android.text.Editable;

/******************************************************************************
 ** File name: TSJniUtil                                                     **
 ** Creation date: 18-8-12                                                   **
 ** Author: Junxin Gao                                                       **
 ** Description:                                                             **
 **           Copyright (c) 2018 ThunderSoft All rights reserved.            **
 ******************************************************************************/
public class TSJniUtil {
    public static final int LINE_G1 = 0;
    public static final int LINE_G2 = 1;
    public static final int LINE_G3 = 2;
    public static final int LINE_G_DEFAULT = 3;

    public static native int opnousTofInit(int width,
                                           int height,
                                           int rowStride,
                                           float pixStride,
                                           String path);

    public static native int opnousTofProccess(ByteBuffer rawBuffer);

    public static native int opnousTofSetDepthCorrectOn(int isCorrectOn);

    public static native int opnousTofSetVerticalFov(int verticalFov);

    public static native int opnousTofSetHorizontalFov(int horizontalFov);

    public static native int opnousTofGetDepthBuffer(ByteBuffer outDepthBuffer);

    public static native int opnousTofGetPointsBuffer(ByteBuffer outPointsBuffer);

    public static native int opnousTofTerm();

    public static native int opnousTofGetDepthBitmap(ByteBuffer inDepth,
                                                     Bitmap outBitmap,
                                                     int isColor);

    public static native int opnousTofGetRawBitmap(Bitmap outBitmap);

    public static native int opnousTofGetIRBitmap(Bitmap outBitmap);

    public static native int opnousTofGetCroodDepthValue(ByteBuffer inDepth,
                                                         int x,
                                                         int y);

    public static native int opnousTofGetTemperature(int gMode, int offset);

    static {
        System.loadLibrary("OpnousTof");
    }
}