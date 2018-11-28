package com.thundersoft.opnoustofdemo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

/******************************************************************************
 ** File name: SplashActivity                                                **
 ** Creation date: 18-8-23                                                   **
 ** Author: Junxin Gao                                                   **
 ** Description:                                                             **
 **           Copyright (c) 2018 ThunderSoft All rights reserved.            **
 ******************************************************************************/
public class SplashActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int WAIT_CAMERA_CLOSE = 0;
    private static final int START_CAMERA_ACTIVITY = 1;
    private HandlerThread mBackgroundThread;
    private Handler mHandler;

    private static final int REQUEST_PERMISSION = 1;
    private static final String[] TOF_PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
    };

    private void requestPermission(String[] permissions) {
        ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), "no permission", Toast.LENGTH_SHORT).show();
                    finish();
                    System.exit(1);
                    return;
                }
            }
            if (!isFinishing()) {
                Intent intent = new Intent(SplashActivity.this, TofCameraActivity.class);
                startActivity(intent);
                finish();
            }
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        setBackgroundThread();
        while (!TofCameraActivity.isCameraClose) {
            mHandler.sendMessageDelayed(mHandler.obtainMessage(WAIT_CAMERA_CLOSE), 1000);
        }　　　　　　　　　　　　　　　　　　　　
        mHandler.sendMessageDelayed(mHandler.obtainMessage(START_CAMERA_ACTIVITY), 1500);
    }

    @Override
    public void onClick(View view) {

    }

    private void setBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mHandler = new Handler(mBackgroundThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                super.handleMessage(msg);
                switch (msg.what) {
                    case WAIT_CAMERA_CLOSE:
                        break;
                    case START_CAMERA_ACTIVITY:
                        requestPermission(TOF_PERMISSIONS);
                        break;
                    default:
                        break;
                }

            }
        };
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
