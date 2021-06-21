package com.capybara.contacttracingscanner;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.budiyev.android.codescanner.CodeScanner;
import com.google.zxing.Result;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "ContactTracing";
    private static final String CDC_PHONE_NUM = "1922";
    private static final String CDC_SMS_PREFIX = "smsto:" + CDC_PHONE_NUM;
    private static final int RC_PERMISSION = 10;

    private CodeScanner mCodeScanner;
    private boolean mCameraPermissionGranted;
    private boolean mLocationPermissionGranted;
    private boolean mNeedBootUpCheck;

    private void composeSmsMessage(String message) {
        //TODO: We should check if the message contains the valid location code in the future
        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse(CDC_SMS_PREFIX));
        intent.putExtra("sms_body", message);
        if (intent.resolveActivity(getPackageManager()) != null) {
            Log.d(TAG, "Ready to send sms");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    private void handleResponse(Result result) {
        String resultStr = result.getText();
        if(resultStr != null && resultStr.length() > CDC_SMS_PREFIX.length() &&
                resultStr.toLowerCase().startsWith(CDC_SMS_PREFIX)) {
            Log.d(TAG, "Retrieve CDC location code");
            composeSmsMessage(resultStr.substring(CDC_SMS_PREFIX.length() + 1));
        } else {
            Log.d(TAG, "Retrieve unknown QR code result");
            runOnUiThread(() -> {
                ScanResultDialog dialog = new ScanResultDialog(this, result);
                dialog.setOnDismissListener(d -> startPreview());
                dialog.show();
            });
        }
    }

    private void startPreview() {
        if (mCameraPermissionGranted && !mNeedBootUpCheck) {
            mCodeScanner.startPreview();
        }
    }

    private void stopPreview() {
        mCodeScanner.releaseResources();
    }

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
        if(locationManager == null) {
            Log.e(TAG, "Cannot get the location manager");
            return false;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
            return locationManager.isLocationEnabled();
        else
            return locationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER);
    }

    private boolean isBluetoothEnabled() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter == null) {
            Log.e(TAG, "Cannot get the bluetooth adaptor");
            return false;
        }

        return bluetoothAdapter.isEnabled();
    }

    private boolean isExposureServiceInstalled() {
        try {
            PackageInfo info =
                    getPackageManager().getPackageInfo(getString(R.string.cdc_exposure_notification),
                            PackageManager.GET_META_DATA);
            if(info != null) {
                Log.d(TAG,"CDC Exposure Service Installed");
                return true;
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.d(TAG,"CDC Exposure Service Not Installed");
        }

        return false;
    }

    private void bootUpCheck() {
        if(!mNeedBootUpCheck)
            return;

        boolean exposureServiceInstalled = isExposureServiceInstalled();
        boolean locationEnabled = isLocationEnabled();
        boolean btEnabled = isBluetoothEnabled();

        if(!exposureServiceInstalled) {
            Log.d(TAG, "Recommend the user to install the social distancing app");
            runOnUiThread(() -> {
                RecommendInstallDialog dialog = new RecommendInstallDialog(this, this);
                dialog.setOnDismissListener(d -> {
                    mNeedBootUpCheck = false;
                    startPreview();
                });
                dialog.show();
            });

            return;
        }

        if(mLocationPermissionGranted && (!locationEnabled) || (!btEnabled)) {
            Log.d(TAG, "Prompt the user to enable the exposure notification service");
            runOnUiThread(() -> {
                EnableServiceDialog dialog = new EnableServiceDialog(this, this);
                dialog.setOnDismissListener(d -> {
                    mNeedBootUpCheck = false;
                    startPreview();
                });
                dialog.show();
            });

            return;
        }

        mNeedBootUpCheck = false;
    }

    private void requestPermission() {
        ArrayList<String> permission = new ArrayList<>();

        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            mCameraPermissionGranted = false;
            permission.add(Manifest.permission.CAMERA);
        } else {
            mCameraPermissionGranted = true;
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            mLocationPermissionGranted = true;
        } else {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionGranted = false;
                permission.add(Manifest.permission.ACCESS_FINE_LOCATION);
            } else {
                mLocationPermissionGranted = true;
            }
        }

        if(permission.size() > 0)
            requestPermissions(permission.toArray(new String[0]), RC_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != RC_PERMISSION) {
            Log.w(TAG, "The permission callback is not triggered by this activity");
            return;
        }

        int index = 0;
        for(String permission : permissions) {
            boolean granted = (grantResults[index] == PackageManager.PERMISSION_GRANTED);

            Log.d(TAG, permission + ":" + granted);
            switch (permission) {
                case Manifest.permission.CAMERA:
                    mCameraPermissionGranted = granted;
                    startPreview();
                    break;
                case Manifest.permission.ACCESS_FINE_LOCATION:
                    mLocationPermissionGranted = granted;
                    break;
                default:
                    Log.w(TAG, "Unknown permission result:" + permission);
            }

            index++;
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        mCodeScanner = new CodeScanner(this, findViewById(R.id.scanner));
        mCodeScanner.setDecodeCallback(this::handleResponse);
        mCodeScanner.setErrorCallback(error -> runOnUiThread(
                () -> Toast.makeText(this, getString(R.string.scanner_error, error), Toast.LENGTH_LONG).show()));

        requestPermission();

        mNeedBootUpCheck = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");

        bootUpCheck();

        startPreview();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause()");

        stopPreview();

        mNeedBootUpCheck = true;

        super.onPause();
    }
}