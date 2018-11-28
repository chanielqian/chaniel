package com.thundersoft.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import com.thundersoft.opnoustofdemo.TofCameraActivity;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


/******************************************************************************
 ** File name: TofView                                                       **
 ** Creation date: 18-8-12                                                   **
 ** Author: Junxin Gao                                                       **
 ** Description:                                                             **
 **           Copyright (c) 2018 ThunderSoft All rights reserved.            **
 ******************************************************************************/
public class TofView extends GLSurfaceView {
    public interface OnTouchCroodListener {
        void onCroodAvailable(float x, float y);
    }

    private Paint mPaint;
    private Bitmap mCanvasBitmap;
    private int mBitmapWidth;
    private int mBitmapHeight;
    private long lastTime = 0;
    private int mCount = 0;
    private final String TAG = "TOFView";
    private boolean is3DRender = false;
    private long lastKeyUpTime = 0;
    private int mInZoom = 0;
    private int mPrimaryPoint = 0;
    private int mSecondaryPoint = 0;
    private float mPrimaryX = 0;
    private float mPrimaryY = 0;
    private float mSecondaryX = 0;
    private float mSecondaryY = 0;
    private float mTouchX = 0;
    private float mTouchY = 0;
    private float mRight = 0;
    private float mBottom = 0;
    private float mTop = 0;
    private float mLeft = 0;

    public void setIsDrawPoint(boolean drawPoint) {
        isDrawPoint = drawPoint;
    }

    private boolean isDrawPoint = false;
    private TofRender mRender;
    private Canvas mCanvas;
    private OnTouchCroodListener touchCroodListener;

    public TofView(Context context) {
        super(context);
        initialize();
    }

    public void setOnTouchCroodListener(OnTouchCroodListener onTouchCroodListener) {
        touchCroodListener = onTouchCroodListener;
    }

    public void setIs3DRender(boolean flag) {
        mRender.setPCDraw(flag);
        is3DRender = flag;
    }

    public TofView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    private void initialize() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(10f);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.BLACK);
        setEGLContextClientVersion(2);
        mRender = new TofRender();
        setRenderer(mRender);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    public void clearScreen() {
        Log.d(TAG, "clearScreen");
        postInvalidate();
    }

    public void setBitmap(Bitmap bitmap) {
        if (is3DRender) {
            mCanvasBitmap = null;
            return;
        }
        mCount++;
        if (mCount == 30) {
            long current = System.currentTimeMillis();
            Log.i(TAG, "+++++++++++FPS: " + 1000 / ((current - lastTime) / 30) + "+++++++++++ view width:"
                    + this.getWidth() + " height:" + this.getHeight());
            lastTime = current;
            mCount = 0;
        }
        if (bitmap == null) {
            Log.e(TAG, "bitmap is null");
        }
        this.mCanvasBitmap = bitmap;
        mBitmapWidth = bitmap.getWidth();
        mBitmapHeight = bitmap.getHeight();
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mCanvas = canvas;
        if (touchCroodListener != null) {
            float x = mTouchX / this.getWidth() * TofCameraActivity.mWidth;
            float y = mTouchY / this.getHeight() * TofCameraActivity.mHeight;
            touchCroodListener.onCroodAvailable(x, y);
        }
        if (is3DRender) {
            Log.d(TAG, "Select 3D view mode");
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        } else {
            if (mCanvasBitmap != null) {
                int minL = mBitmapHeight;
                int maxL = mBitmapWidth;
                Matrix matrix = new Matrix();
                int coreY = minL / 2;
                int coreX = maxL / 2;
                float scaleX = this.getWidth() / (float) maxL;
                float scaleY = this.getHeight() / (float) minL;
                matrix.setRotate(0, coreX, coreY);
                matrix.postScale(scaleX, scaleY);
                canvas.drawBitmap(mCanvasBitmap, matrix, mPaint);
            } else {
                Log.e(TAG, "mCanvasBitmap = null");
            }
            if (isDrawPoint) {
                float mRectWidth = (this.getWidth() / TofCameraActivity.mWidth + this.getHeight() / TofCameraActivity.mHeight) * 5;
                mLeft = mTouchX - mRectWidth;
                mRight = (mTouchX + mRectWidth);
                mTop = mTouchY - mRectWidth;
                mBottom = (mTouchY + mRectWidth);
                Log.d(TAG, "draw rect (" + mTouchX + "," + " " + mTouchY + ") mLeft : " + mLeft + " mRight: " + mRight + " mTop: " + mTop + " mBottom: " + mBottom + "");
                canvas.drawRect(mLeft, mTop, mRight, mBottom, mPaint);
            }
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!is3DRender) {
            if (touchCroodListener != null) {
                mTouchX = event.getX();
                mTouchY = event.getY();
            }
            return true;
        }
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            mPrimaryPoint = event.getActionIndex();
            mPrimaryX = event.getX(mPrimaryPoint);
            mPrimaryY = event.getY(mPrimaryPoint);
            Log.e(TAG, "MotionEvent.ACTION_DOWN, x:" + mPrimaryX + " y:" + mPrimaryY);
        } else if (event.getActionMasked() == MotionEvent.ACTION_POINTER_DOWN) {
            Log.d(TAG, "MotionEvent.ACTION_POINTER_DOWN");
            if (mInZoom == 0) {
                mInZoom = 1;
                mSecondaryPoint = event.getActionIndex();
                mSecondaryX = event.getX(mSecondaryPoint);
                mSecondaryY = event.getY(mSecondaryPoint);
            }
        } else if (event.getActionMasked() == MotionEvent.ACTION_MOVE) {
            Log.d(TAG, "MotionEvent.ACTION_MOVE");
            if (1 == mInZoom) {
                float ox1 = mPrimaryX;
                float oy1 = mPrimaryY;
                float ox2 = mSecondaryX;
                float oy2 = mSecondaryY;

                float nx1 = event.getX(mPrimaryPoint);
                float ny1 = event.getY(mPrimaryPoint);
                float nx2 = event.getX(mSecondaryPoint);
                float ny2 = event.getY(mSecondaryPoint);

                double old_dist = Math.sqrt((ox2 - ox1) * (ox2 - ox1) + (oy2 - oy1) * (oy2 - oy1));
                double new_dist = Math.sqrt((nx2 - nx1) * (nx2 - nx1) + (ny2 - ny1) * (ny2 - ny1));


                if (Math.abs(new_dist - old_dist) > 5) {
                    if (new_dist > old_dist) {
                        mRender.scale(1.02f);
                    } else {
                        mRender.scale(1.0f / 1.02f);
                    }
                    mPrimaryX = event.getX(mPrimaryPoint);
                    mPrimaryY = event.getY(mPrimaryPoint);
                    mSecondaryX = event.getX(mSecondaryPoint);
                    mSecondaryY = event.getY(mSecondaryPoint);
                    requestRender();
                }

            } else if (0 == mInZoom) {
                float moveX = event.getX(mPrimaryPoint) - mPrimaryX;
                float moveY = event.getY(mPrimaryPoint) - mPrimaryY;

                mRender.rotate(moveX / 10, moveY / 10);  /*To slow down the rotate*/
                requestRender();

                mPrimaryX += moveX;
                mPrimaryY += moveY;
            }
        } else if (event.getActionMasked() == MotionEvent.ACTION_POINTER_UP) {
            Log.d(TAG, "MotionEvent.ACTION_POINTER_UP");
            mInZoom = 2;
        } else if (event.getActionMasked() == MotionEvent.ACTION_UP) {
            mInZoom = 0;
            if ((event.getEventTime() - lastKeyUpTime) < 400) {
                mRender.resetViewPort();
                requestRender();
            }
            lastKeyUpTime = event.getEventTime();
            Log.d(TAG, "MotionEvent.ACTION_UP, x:" + mPrimaryX + " y:" + mPrimaryY);
        }
        return true;
    }
}
