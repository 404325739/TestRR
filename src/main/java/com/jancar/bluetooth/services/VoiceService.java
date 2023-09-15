package com.jancar.bluetooth.services;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import com.jancar.bluetooth.BtApplication;
import com.jancar.bluetooth.R;
import com.jancar.bluetooth.view.ClipCircleView;
import com.jancar.btservice.bluetooth.IBtControlVoiceCallback;
import com.jancar.sdk.utils.Logcat;

/**
 * 蓝牙语音服务.
 */
@SuppressLint("SetTextI18n")
public class VoiceService extends Service {
    private static final int MSG_SHOWFLOAT = 2;
    private static final int MSG_REMOVEFLOAT = 3;
    private static final int MSG_DRAW_CIRCLE = 4;
    private final IBtControlVoiceCallback.Stub mIBtControlVoiceCallback = new IBtControlVoiceCallback.Stub() {
        @Override
        public void onOpenSuccess(String msg) throws RemoteException {
            Logcat.d("" + msg);
            handler.sendEmptyMessage(MSG_SHOWFLOAT);
        }

        @Override
        public void onOpenFailure(int errorCode) throws RemoteException {
            Logcat.d("" + errorCode);
        }

        @Override
        public void onCloseSuccess(String msg) throws RemoteException {
            Logcat.d("" + msg);
            handler.sendEmptyMessage(MSG_REMOVEFLOAT);
        }

        @Override
        public void onCloseFailure(int errorCode) throws RemoteException {
            Logcat.d("" + errorCode);
        }
    };
    private ClipCircleView clipCircleView;
    private int radius;
    private boolean forceClose = false;
    private View rootView;
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mLayoutParams;
    private final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_SHOWFLOAT:
                    showFloatingWindow();
                    break;
                case MSG_REMOVEFLOAT:
                    removeMessages(MSG_DRAW_CIRCLE);
                    radius = 0;
                    removeFloatingWindow();
                    break;
                case MSG_DRAW_CIRCLE:
                    drawCircle();
                    break;

            }

        }
    };

    private void drawCircle() {
        if (radius < 6) {
            radius++;
        } else {
            radius = 0;
        }
        clipCircleView.initPath(radius).invalidate();
        handler.sendEmptyMessageDelayed(MSG_DRAW_CIRCLE, 250);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            if ("jancar.action.bluetooth_voice".equals(intent.getAction())) {
                try {
                    BtApplication.getInstance().getBluetoothManager().openMotePhoneVoice(mIBtControlVoiceCallback);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    public boolean initBtFloat() {
        if (mWindowManager == null) {
            mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        }

        if (mLayoutParams == null) {
            final Point screenSize = new Point();
            mWindowManager.getDefaultDisplay().getRealSize(screenSize);
            mLayoutParams = new WindowManager.LayoutParams();
            mLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            mLayoutParams.format = PixelFormat.RGBA_8888;
            mLayoutParams.gravity = Gravity.CENTER;
            mLayoutParams.width = screenSize.x / 3;
            mLayoutParams.height = screenSize.y / 3;
        }


        if (rootView == null) {
            rootView = LayoutInflater.from(this).inflate(R.layout.voice_float, null);
        }
        if (clipCircleView == null) {
            clipCircleView = rootView.findViewById(R.id.circleView);
            try {
                clipCircleView.init(this);
            } catch (Exception e) {
                e.printStackTrace();
            }

            rootView.setOnClickListener(v -> {
                Logcat.d("click");
                BtApplication.getInstance().getBluetoothManager().closeMotePhoneVoice(mIBtControlVoiceCallback);
                forceClose = true;
            });
        }

        return mWindowManager != null && mLayoutParams != null && rootView != null;
    }

    private void showFloatingWindow() {
        initBtFloat();
        try {
            if (!rootView.isShown()) {
                mWindowManager.addView(rootView, mLayoutParams);
                drawCircle();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void removeFloatingWindow() {

        try {
            boolean needClose = rootView.isShown();
            if (needClose || forceClose) {
                forceClose = false;
                mWindowManager.removeView(rootView);
            }
        } catch (Exception e) {
            Logcat.e("" + e.getClass());
        }

    }


}
