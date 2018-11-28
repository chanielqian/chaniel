package com.thundersoft.view;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import java.io.File;
import java.nio.ByteBuffer;
/******************************************************************************
 ** File name: TofView                                                       **
 ** Creation date: 18-8-12                                                   **
 ** Author: Junxin Gao                                                       **
 ** Description:                                                             **
 **           Copyright (c) 2018 ThunderSoft All rights reserved.            **
 ******************************************************************************/

public class PointCloudController {
    private TofView mView;

    private PointCloudController() {
    }

    static public PointCloudController getInstance() {
        if (null == m_instance) {
            m_instance = new PointCloudController();
        }
        return m_instance;
    }

    public void setView(TofView view) {
        mView = view;
    }

    public void init(Activity current) {
        mCurrentActivity = current;
    }

    public void loadOpnousPCData(ByteBuffer buffer) {
        if (mPointCloud == null) {
            mPointCloud = new OpnousPointCloudData(buffer);
        } else {
            mPointCloud.loadBuffer(buffer);
        }
        if (mView != null) {
            GLPerspective.setPreferredScale(1.0f, 1.0f, 1.0f);
            mView.setIs3DRender(true);
            mView.requestRender();
        } else {
            Log.e("gjx", "view is null");
        }
    }

    public PointCloudData getPointCloudData() {
        return mPointCloud;
    }

    static private PointCloudController m_instance = null;

    private PointCloudData mPointCloud = null;

    private Activity mCurrentActivity;
}
