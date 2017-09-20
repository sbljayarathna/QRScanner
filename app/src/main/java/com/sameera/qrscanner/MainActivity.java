package com.sameera.qrscanner;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    public static final String KEY = MainActivity.class.getSimpleName().toString();
    public static final String TAG = MainActivity.class.getSimpleName().toString();
    public static final int CAMERA_STATUS = 102;

    private CameraSource mCameraSource;

    @BindView(R.id.activity_scan_card_camera_view) SurfaceView mCameraView;
    @BindView(R.id.et_scan) EditText etScan;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        requestPermission();
    }


    private void requestPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_STATUS);
            }
        } else {
            //setup camera to scan QR - Sameera
            setUpQRScanner();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "+---------> CAMERA_STATUS onRequestPermissionsResult is called <---------+");
        switch (requestCode) {
            case CAMERA_STATUS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //setup camera to scan QR - Sameera
                    setUpQRScanner();

                    //start the camera to scan the QR if the permission is granted - Sameera
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    try {
                        //start the camera - Sameera
                        mCameraSource.start(mCameraView.getHolder());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Log.d(TAG, "+---------> Camera Permission Granted <---------+");

                } else {
                    boolean showRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0]);
                    if (showRationale) {
                        Log.d(TAG, "+---------> Camera Permission Denied <---------+");
                        Log.d(TAG, "Check box unchecked");
                        Toast.makeText(this, getString(R.string.msg_request_permission_camera), Toast.LENGTH_SHORT).show();
                    } else {
                        Log.d(TAG, "+---------> Camera Permission Denied <---------+");
                        Log.d(TAG, "Check box checked");
                        Toast.makeText(this, getString(R.string.msg_request_permission_camera), Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    //setting up the QR scanner - Sameera
    private void setUpQRScanner() {
        BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.ALL_FORMATS)
                .build();

        mCameraSource = new CameraSource.Builder(this, barcodeDetector).build();

        Log.d(TAG, "+---------> setUpQRScanner <---------+");
        mCameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    Log.d(TAG, "+---------> surfaceCreated <---------+");
                    if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }

                    Log.d(TAG, "+---------> start <---------+");
                    mCameraSource.start(mCameraView.getHolder());

                } catch (IOException ie) {
                    Log.e("CAMERA SOURCE", ie.getMessage());
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                mCameraSource.stop();
            }
        });


        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();

                if (barcodes.size() != 0) {
                    Log.d(TAG, barcodes.valueAt(0).displayValue);
                    etScan.post(() -> {
                        try {

                            String mCardNumber = barcodes.valueAt(0).displayValue;

                            // Update the EditText - Sameera
                            etScan.setText(mCardNumber);

                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    });
                }
            }
        });
    }
}
