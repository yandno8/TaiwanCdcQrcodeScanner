package com.capybara.contacttracingscanner;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

public class EnableServiceDialog extends BaseDialog {
    public EnableServiceDialog(@NonNull Activity activity, @NonNull Context context) {
        super(context);
        setContentView(R.layout.dialog_enable_service);

        //noinspection ConstantConditions
        findViewById(R.id.yes_btn).setOnClickListener(v -> {
            ComponentName componetName = new ComponentName(
                    context.getString(R.string.cdc_exposure_notification),
                    context.getString(R.string.cdc_exposure_notification_activity));

            Intent intent = new Intent();
            intent.setComponent(componetName);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);

            activity.finish();
        });

        //noinspection ConstantConditions
        findViewById(R.id.no_btn).setOnClickListener(v -> dismiss());
    }
}
