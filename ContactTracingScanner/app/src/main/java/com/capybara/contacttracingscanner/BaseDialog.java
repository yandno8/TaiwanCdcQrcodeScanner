package com.capybara.contacttracingscanner;

import android.content.Context;
import android.util.TypedValue;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDialog;

public class BaseDialog extends AppCompatDialog {
    public BaseDialog(@NonNull Context context) {
        super(context, resolveDialogTheme(context));
    }

    private static int resolveDialogTheme(@NonNull Context context) {
        TypedValue outValue = new TypedValue();
        context.getTheme().resolveAttribute(androidx.appcompat.R.attr.alertDialogTheme, outValue, true);
        return outValue.resourceId;
    }
}
