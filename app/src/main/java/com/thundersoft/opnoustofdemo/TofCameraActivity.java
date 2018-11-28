package com.thundersoft.opnoustofdemo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.media.Image;
import android.media.ImageReader;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.thundersoft.TSJniUtil;
import com.thundersoft.utils.AnimationUtils;
import com.thundersoft.view.PointCloudController;
import com.thundersoft.view.TofView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/******************************************************************************
 ** File name: TofCameraActivity                                             **
 ** Creation date: 18-8-12                                                   **
 ** Author: Junxin Gao                                                       **
 ** Description:                                                             **
 **           Copyright (c) 2018 ThunderSoft All rights reserved.            **
 ******************************************************************************/
public class TofCameraActivity extends AppCompatActivity implements View.OnClickListener {
    private TofView mTofView;
    private ImageReader mImageReader;
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;
    private Handler mTOFHandler;
    private HandlerThread mTOFThread;
    private Handler mViewHandler;
    private HandlerThread mViewThread;
    private Object mLockTOF = new Object();
    private Object mLockView = new Object();
    private static final int HANDLE_MSG_TOF_PROCESS = 4;
    private static final int HANDLE_MSG_EXIT_PROCESS = 3;
    private static final int HANDLE_MSG_NO_COEFFILE = 5;
    private static final int HANDLE_MSG_VIEW_PROCESS = 1;
    private static final int HANDLE_MSG_GLVIEW_PROCESS = 2;
    public static final int mWidth = 328;
    public static final int mRawWidth = 328;
    public static final int mHeight = 248;
    public static final int mRawHeight = 744;
    public static final int mRawRowStride = 496;
    public static final float mRawPixStride = 1.5f;
    private String TAG = "OpnousTOF";
    private CameraDevice mCameraDevice;
    private CaptureRequest.Builder mRequestBuilder;
    private Semaphore mCameraOpenCloseLock = new Semaphore(1);
    private CameraCaptureSession mCaptureSession;
    private Button mBtnback;
    private static boolean isCapture = false;
    private static String mPath;
    private Button mBtnCapture;
    private Switch mDistValueSwitch;
    private TextView mDistTextView;
    private final PointCloudController controller = PointCloudController.getInstance();
    private FrameLayout mSelectLayout;
    private Button mBtnDepth;
    private Button mBtnOthers;
    private Button mBtn3D;
    private Button mBtnIR;
    public static boolean isCameraClose = true;

    public static final int DEPTH_VIEW = 0;
    public static final int RAW_VIEW = 3;
    public static final int IR_VIEW = 1;
    public static final int POINT_CLOUD_VIEW = 2;
    private static int mTouchX = 0;
    private static int mTouchY = 0;
    private static int mCroodDistValue;
    public static int mViewMode = DEPTH_VIEW;
    private static boolean isDistValue = false;
    private static int mTemperature;
    private static int mLineGMode = TSJniUtil.LINE_G2;

    private FrameLayout mFunction_select;
    //Function Btn
    private Button mBtn_more;
    private Button mSetOffsetTemp;
    private Button mSetOffsetDis;
    private Button mSetVerticalFov;
    private Button mSetHorizontalFov;
    private Switch mSwitchCalibration;
    //capture
    private Button mDoCalibrationButton;

    //EditText
    private EditText mOffsetTempText;
    private EditText mOffsetDisText;
    private EditText mVerticalFovText;
    private EditText mHorizontalFovText;
    //offseTemperature
    private int mOffseTempValue = 460;
    //offsetDistance
    private int mOffsetDisValue = 30;
    private int mHorizontalFovValue = 73;
    private int mVerticalFovValue = 53;

    private int mRateCount = 0;
    //sum temperature
    private int mSumTemperature;
    //AVG
    private DecimalFormat mDecimalFormat = new DecimalFormat("######0");

    private boolean isCorrectOn = false;

    private String mCsvPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTofView = findViewById(R.id.ts_tof_view);
        mBtnback = findViewById(R.id.btn_return);
        mDistTextView = findViewById(R.id.textView_dist);
        mDistTextView.setOnClickListener(this);
        mDistValueSwitch = findViewById(R.id.switch_dist);
        mDistValueSwitch.setOnCheckedChangeListener(mSwitchListener);
        mSelectLayout = findViewById(R.id.mode_select);
        mBtnback.setOnClickListener(this);
        mBtnCapture = findViewById(R.id.btn_capture);
        mBtnCapture.setOnClickListener(this);
        mBtn3D = findViewById(R.id.btn_3d);
        mBtnDepth = findViewById(R.id.btn_depth);
        mBtnIR = findViewById(R.id.btn_ir);
        mBtnOthers = findViewById(R.id.btn_raw);
        mBtn3D.setOnClickListener(this);
        mBtnDepth.setOnClickListener(this);
        mBtnIR.setOnClickListener(this);
        mBtnOthers.setOnClickListener(this);
        controller.init(this);
        mSelectLayout.setVisibility(View.INVISIBLE);
        controller.setView(mTofView);
        mTofView.setOnTouchCroodListener(mOnTouchCroodListener);

        mFunction_select = findViewById(R.id.function_select);
        mFunction_select.setVisibility(View.INVISIBLE);
        mBtn_more = findViewById(R.id.btn_more);

        mBtn_more.setOnClickListener(this);

        mSetOffsetTemp = findViewById(R.id.setOffsetTemperature);
        mSetOffsetDis = findViewById(R.id.setoffsetDistance);
        mSetHorizontalFov = findViewById(R.id.btn_setHorizontalFov);
        mSetVerticalFov = findViewById(R.id.btn_setVerticalFov);
        mSwitchCalibration = findViewById(R.id.switch_calibration);

        mDoCalibrationButton = findViewById(R.id.btn_setCalibration);
        mSetOffsetTemp.setOnClickListener(this);
        mSetOffsetDis.setOnClickListener(this);
        mDoCalibrationButton.setOnClickListener(this);
        mSetHorizontalFov.setOnClickListener(this);
        mSetVerticalFov.setOnClickListener(this);
        mSwitchCalibration.setOnCheckedChangeListener(mSwitchListener);

        mOffsetTempText = findViewById(R.id.offsetTemperature);
        mOffsetDisText = findViewById(R.id.offsetDistance);
        mVerticalFovText = findViewById(R.id.et_VerticalFov);
        mHorizontalFovText = findViewById(R.id.et_HorizontalFov);

        startThread();
        mCsvPath = "/sdcard/coef_SB_No10.csv";
        File file = new File(mCsvPath);
        if (!file.exists()) {
            mBackgroundHandler.sendMessage(mBackgroundHandler.obtainMessage(HANDLE_MSG_NO_COEFFILE));
        } else {
            TSJniUtil.opnousTofInit(mWidth, mHeight, mRawRowStride, mRawPixStride, mCsvPath);
            initImageReader(mRawWidth, mRawHeight);
            openCamera(0);
        }

    }


    @Override
    protected void onResume() {
        super.onResume();
        submitRequest();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopRepeating();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCameraDevice != null) {
            mBackgroundHandler.sendMessage(mBackgroundHandler.obtainMessage(HANDLE_MSG_EXIT_PROCESS));
        }
    }

    private final TofView.OnTouchCroodListener mOnTouchCroodListener
            = new TofView.OnTouchCroodListener() {
        @Override
        public void onCroodAvailable(float x, float y) {
            if (isDistValue) {
                mTouchX = (int) x;
                mTouchY = (int) y;
                String lineMode;
                switch (mLineGMode) {
                    case TSJniUtil.LINE_G1:
                        lineMode = " (G1)";
                        break;
                    case TSJniUtil.LINE_G2:
                        lineMode = " (G2)";
                        break;
                    case TSJniUtil.LINE_G3:
                        lineMode = " (G3)";
                        break;
                    case TSJniUtil.LINE_G_DEFAULT:
                        lineMode = "(AVG)";
                        break;
                    default:
                        lineMode = "(AVG)";
                        break;
                }
                String distStr = String.format(Locale.US, "%5dmm %2d℃" + lineMode, mCroodDistValue, mTemperature);
                mDistTextView.setText(distStr);
            }
        }
    };

    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener
            = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            Image image = reader.acquireLatestImage();
            if (image != null) {
                ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                synchronized (mLockTOF) {
                    if (buffer != null) {
                        mTOFHandler.sendMessage(mTOFHandler.obtainMessage(HANDLE_MSG_TOF_PROCESS, buffer));
                        buffer.clear();
                    }
                }
                image.close();
            }

        }
    };

    public static void saveRAWFile(String path, ByteBuffer buffer) {
        try {
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            FileOutputStream output = new FileOutputStream(new File(path));
            output.write(bytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveBitmap(Bitmap mBitmap) {
        String filePath = mPath + ".jpg";
        File f = new File(filePath);
        try {
            f.createNewFile();
        } catch (IOException e) {
        }
        FileOutputStream fOut = null;
        try {
            fOut = new FileOutputStream(f);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
        try {
            fOut.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            fOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + filePath)));
        Toast.makeText(this, "Picture save at " + filePath, Toast.LENGTH_SHORT).show();
    }

    private void initImageReader(int width, int height) {
        mImageReader = ImageReader.newInstance(width, height, ImageFormat.RAW12, 2);
        mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, mBackgroundHandler);
    }

    private long mLastTime;

    private void startThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case HANDLE_MSG_EXIT_PROCESS:
                        TSJniUtil.opnousTofTerm();
                        closeCamera();
                        stopThread();
                        break;
                    case HANDLE_MSG_NO_COEFFILE:
                        try {
                            Toast.makeText(TofCameraActivity.this, mCsvPath + ",矫正文件不存在", Toast.LENGTH_LONG).show();
                            Thread.sleep(1000);
                            finish();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        break;
                    default:
                        break;
                }
            }
        };

        mTOFThread = new HandlerThread("TOFThread");
        mTOFThread.start();
        mTOFHandler = new Handler(mTOFThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case HANDLE_MSG_TOF_PROCESS:
                        synchronized (mLockTOF) {
                            ByteBuffer buffer = (ByteBuffer) msg.obj;
                            if (buffer != null) {
                                int ret = 0;
                                ByteBuffer outDepth = ByteBuffer.allocateDirect(mWidth * mHeight * 2);
                                ByteBuffer outPoints = ByteBuffer.allocateDirect(mWidth * mHeight * 4 * 3);
                                ret = TSJniUtil.opnousTofProccess(buffer);
                                long currentTime = System.currentTimeMillis();
                                Log.d(TAG, "handleMessage: time:" + (currentTime - mLastTime));
                                mLastTime = currentTime;
                                if (ret == 0) {
                                    ret = TSJniUtil.opnousTofGetDepthBuffer(outDepth);
                                    ret = TSJniUtil.opnousTofGetPointsBuffer(outPoints);
                                } else {
                                    Log.e(TAG, "opnousTofProccess failed");
                                }
                                if (isCapture) {
                                    saveRAWFile(mPath + "_depth" + ".raw", outDepth);
                                    saveRAWFile(mPath + "_pointcloud" + ".raw", outPoints);
                                }
                                if (ret == 0) {
                                    synchronized (mLockView) {
                                        if (mViewMode == POINT_CLOUD_VIEW) {
                                            mViewHandler.sendMessage(mViewHandler.obtainMessage(HANDLE_MSG_GLVIEW_PROCESS, outPoints));
                                        } else {
                                            mViewHandler.sendMessage(mViewHandler.obtainMessage(HANDLE_MSG_VIEW_PROCESS, outDepth));
                                        }
                                    }
                                }
                                if (isDistValue) {
                                    mRateCount++;
                                    mCroodDistValue = TSJniUtil.opnousTofGetCroodDepthValue(outDepth, mTouchX, mTouchY) - mOffsetDisValue;
                                    mSumTemperature += TSJniUtil.opnousTofGetTemperature(mLineGMode, mOffseTempValue);
                                    //30 frame rate average
                                    if (mRateCount == 30) {
                                        double avg = mSumTemperature / (double) mRateCount;
                                        mTemperature = Integer.parseInt(mDecimalFormat.format(avg));
                                        mSumTemperature = 0;
                                        mRateCount = 0;
                                    }
                                    Log.d(TAG, " Dist value: " + mCroodDistValue + ", temperature: " + mTemperature);
                                }
                            }
                        }
                        break;
                    default:
                        break;
                }
            }
        };
        mViewThread = new HandlerThread("TOFView");
        mViewThread.start();
        mViewHandler = new Handler(mViewThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case HANDLE_MSG_VIEW_PROCESS:
                        ByteBuffer outDepth = (ByteBuffer) msg.obj;
                        synchronized (mLockView) {
                            if (outDepth != null) {
                                Bitmap bitmap;
                                switch (mViewMode) {
                                    case DEPTH_VIEW:
                                        bitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
                                        TSJniUtil.opnousTofGetDepthBitmap(outDepth, bitmap, 1);
                                        break;
                                    case IR_VIEW:
                                        bitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
                                        TSJniUtil.opnousTofGetIRBitmap(bitmap);
                                        break;
                                    case RAW_VIEW:
                                        bitmap = Bitmap.createBitmap(mRawWidth, mRawHeight, Bitmap.Config.ARGB_8888);
                                        TSJniUtil.opnousTofGetRawBitmap(bitmap);
                                        break;
                                    default:
                                        bitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
                                        TSJniUtil.opnousTofGetDepthBitmap(outDepth, bitmap, 0);
                                        break;
                                }
                                mTofView.setIs3DRender(false);
                                mTofView.setBitmap(bitmap);
                                if (isCapture) {
                                    saveBitmap(bitmap);
                                    isCapture = false;
                                }
                            }

                        }
                        break;
                    case HANDLE_MSG_GLVIEW_PROCESS:
                        ByteBuffer outPoints = (ByteBuffer) msg.obj;
                        synchronized (mLockView) {
                            if (outPoints != null) {
                                if (mViewMode == POINT_CLOUD_VIEW) {
                                    controller.loadOpnousPCData(outPoints);
                                }
                            }
                            if (isCapture) {
                                isCapture = false;
                                Log.d(TAG, "Point cloud view not support capture");
                                //Toast.makeText(this, "Point cloud view not support capture", Toast.LENGTH_SHORT).show();
                            }
                        }
                        break;
                }
            }
        };

    }

    private void stopThread() {
        mTOFThread.quitSafely();
        mBackgroundThread.quitSafely();
        mViewThread.quitSafely();

        try {
            mTOFThread.join();
            mTOFThread = null;
            mTOFHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        try {
            mViewThread.join();
            mViewThread = null;
            mViewThread = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void stopRepeating() {
        if (mCaptureSession == null) {
            Log.e(TAG, "mCaptureSession is null");
            return;
        }
        try {
            mCaptureSession.stopRepeating();
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice cameraDevice) {
            mCameraDevice = cameraDevice;
            startStreaming();
            mCameraOpenCloseLock.release();
        }

        @Override
        public void onDisconnected(CameraDevice cameraDevice) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(CameraDevice cameraDevice, int i) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
            finish();
        }

        @Override
        public void onClosed(@NonNull CameraDevice camera) {
            super.onClosed(camera);
        }
    };

    private void closeCaptureSession() {
        if (mCaptureSession != null) {
            mCaptureSession.close();
            mCaptureSession = null;
        }
    }

    private void startStreaming() {
        if (null == mCameraDevice) {
            return;
        }
        try {
            closeCaptureSession();
            mRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);

            List<Surface> surfaces = new ArrayList<>();
            mRequestBuilder.addTarget(mImageReader.getSurface());
            surfaces.add(mImageReader.getSurface());

            mCameraDevice.createCaptureSession(surfaces,
                    new CameraCaptureSession.StateCallback() {

                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession session) {
                            mCaptureSession = session;
                            submitRequest();
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession session) {


                        }
                    }, mBackgroundHandler);


        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

    private CameraCaptureSession.CaptureCallback mCaptureCallback = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureStarted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, long timestamp, long frameNumber) {
            super.onCaptureStarted(session, request, timestamp, frameNumber);
        }
    };

    private void submitRequest() {
        if (null == mCameraDevice) {
            return;
        }
        if (null == mCaptureSession) {
            return;
        }
        try {
            mCaptureSession.setRepeatingRequest(mRequestBuilder.build(), mCaptureCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void closeCamera() {
        try {
            mCameraOpenCloseLock.acquire();
            closeCaptureSession();
            if (null != mCameraDevice) {
                isCameraClose = false;
                mCameraDevice.close();
                isCameraClose = true;
                mCameraDevice = null;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.");
        } finally {
            mCameraOpenCloseLock.release();
        }
    }

    @SuppressLint("MissingPermission")
    private void openCamera(final int cameraId) {
        CameraManager manager = (CameraManager) this.getSystemService(Context.CAMERA_SERVICE);
        try {
            Log.d(TAG, "tryAcquire");
            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }
            manager.openCamera(String.valueOf(cameraId), mStateCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            Toast.makeText(this, "Cannot access the camera.", Toast.LENGTH_SHORT).show();
            this.finish();
        } catch (NullPointerException e) {
            // Currently an NPE is thrown when the Camera2API is used but not supported on the
            // device this code runs.
               /* ErrorDialog.newInstance(getString(R.string.camera_error))
                        .show(getChildFragmentManager(), FRAGMENT_DIALOG);*/
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera opening.");
        }

    }

    private void resetNormalView() {
        AnimationUtils.showAndHiddenAnimation(mSelectLayout, AnimationUtils.AnimationState.STATE_HIDDEN, 1000);
        mTofView.setIs3DRender(false);
        mTofView.requestRender();
        mBtnCapture.setVisibility(View.VISIBLE);
    }

    private void setPointCloudView() {
        AnimationUtils.showAndHiddenAnimation(mSelectLayout, AnimationUtils.AnimationState.STATE_HIDDEN, 1000);
        mTofView.setIs3DRender(true);
        mTofView.clearScreen();
        mBtnCapture.setVisibility(View.INVISIBLE);
        mDistValueSwitch.setChecked(false);
        mDistTextView.setVisibility(View.GONE);
    }

    private CompoundButton.OnCheckedChangeListener mSwitchListener = new CompoundButton.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            switch (compoundButton.getId()) {
                case R.id.switch_dist:
                    isDistValue = b;
                    mTofView.setIsDrawPoint(b);
                    if (b) {
                        mDistTextView.setText("");
                        mDistTextView.setVisibility(View.VISIBLE);
                        //AnimationUtils.showAndHiddenAnimation(mDistTextView, AnimationUtils.AnimationState.STATE_HIDDEN, 5000);
                    } else {
                        mDistTextView.setText("");
                        mDistTextView.setVisibility(View.INVISIBLE);
                    }
                    break;
                case R.id.switch_calibration:
                    TSJniUtil.opnousTofSetDepthCorrectOn(b ? 1 : 0);
                    AnimationUtils.showAndHiddenAnimation(mFunction_select, AnimationUtils.AnimationState.STATE_HIDDEN, 1000);
                    closeInputKeyboard();
                    break;
                default:
                    break;
            }

        }
    };


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_return:
                AnimationUtils.showAndHiddenAnimation(mSelectLayout, AnimationUtils.AnimationState.STATE_SHOW, 1000);
                break;
            case R.id.btn_capture:
                mPath = "/sdcard/DCIM/TOF_" + System.currentTimeMillis();
                isCapture = true;
                break;
            case R.id.btn_depth:
                mViewMode = DEPTH_VIEW;
                resetNormalView();
                break;
            case R.id.btn_3d:
                mViewMode = POINT_CLOUD_VIEW;
                setPointCloudView();
                break;
            case R.id.btn_ir:
                mViewMode = IR_VIEW;
                resetNormalView();
                break;
            case R.id.btn_raw:
                mViewMode = RAW_VIEW;
                resetNormalView();
                break;
            case R.id.textView_dist:
                mDistTextView.setVisibility(View.VISIBLE);
                mRateCount = 0;
                mSumTemperature = 0;
                mTemperature = 0;
                mLineGMode++;
                mLineGMode = mLineGMode % (TSJniUtil.LINE_G_DEFAULT + 1);
                AnimationUtils.showAndHiddenAnimation(mDistTextView, AnimationUtils.AnimationState.STATE_SHOW, 300);
                break;
            case R.id.btn_more:
                AnimationUtils.showAndHiddenAnimation(mFunction_select, AnimationUtils.AnimationState.STATE_SHOW, 1000);
                break;
            case R.id.setOffsetTemperature:
                String temp = (mOffsetTempText.getText()).toString().trim();
                if (temp.length() == 0) {
                    return;
                }
                AnimationUtils.showAndHiddenAnimation(mFunction_select, AnimationUtils.AnimationState.STATE_HIDDEN, 1000);
                mOffseTempValue = Integer.parseInt(temp);
                closeInputKeyboard();
                break;
            case R.id.setoffsetDistance:
                String dis = (mOffsetDisText.getText()).toString().trim();
                if (dis.length() == 0) {
                    return;
                }
                AnimationUtils.showAndHiddenAnimation(mFunction_select, AnimationUtils.AnimationState.STATE_HIDDEN, 1000);
                mOffsetDisValue = Integer.parseInt(dis);
                closeInputKeyboard();
                break;
            case R.id.btn_setHorizontalFov:
                String horizontalFovStr = (mHorizontalFovText.getText()).toString().trim();
                if (horizontalFovStr.length() == 0) {
                    return;
                }
                AnimationUtils.showAndHiddenAnimation(mFunction_select, AnimationUtils.AnimationState.STATE_HIDDEN, 1000);
                mHorizontalFovValue = Integer.parseInt(horizontalFovStr);
                closeInputKeyboard();
                if (mHorizontalFovValue > 0 && mHorizontalFovValue < 180) {
                    TSJniUtil.opnousTofSetHorizontalFov(mHorizontalFovValue);
                } else {
                    Toast.makeText(TofCameraActivity.this, "Invalid Fov Value", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btn_setVerticalFov:
                String verticalFovStr = (mVerticalFovText.getText()).toString().trim();
                if (verticalFovStr.length() == 0) {
                    return;
                }
                AnimationUtils.showAndHiddenAnimation(mFunction_select, AnimationUtils.AnimationState.STATE_HIDDEN, 1000);
                mVerticalFovValue = Integer.parseInt(verticalFovStr);
                closeInputKeyboard();
                if (mVerticalFovValue > 0 && mVerticalFovValue < 180) {
                    TSJniUtil.opnousTofSetVerticalFov(mVerticalFovValue);
                } else {
                    Toast.makeText(TofCameraActivity.this, "Invalid Fov Value", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

    /**
     * closeInputKeyboard
     */
    public void closeInputKeyboard() {
        //close input keyboard
        InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(mOffsetTempText.getWindowToken(), 0);
    }
}
