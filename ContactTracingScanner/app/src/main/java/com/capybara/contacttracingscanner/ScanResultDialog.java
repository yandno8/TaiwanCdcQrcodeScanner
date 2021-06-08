package com.capybara.contacttracingscanner;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.zxing.Result;

public class ScanResultDialog extends BaseDialog {
    private static final String TAG = "ScanResultDialog";

    public ScanResultDialog(@NonNull Context context, @NonNull Result result) {
        super(context);
        setContentView(R.layout.dialog_scan_result);

        Log.d(TAG, "Retrieving scan result:" + result.getText());

        //noinspection ConstantConditions
        findViewById(R.id.close).setOnClickListener(v -> dismiss());
    }
}
