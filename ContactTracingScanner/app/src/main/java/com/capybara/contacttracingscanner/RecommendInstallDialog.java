package com.capybara.contacttracingscanner;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.NonNull;

public class RecommendInstallDialog extends BaseDialog {
    public RecommendInstallDialog(@NonNull Activity activity, @NonNull Context context) {
        super(context);
        setContentView(R.layout.dialog_recommend_install);

        //noinspection ConstantConditions
        findViewById(R.id.yes_btn).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(
                    "https://play.google.com/store/apps/details?id=" + context.getString(R.string.cdc_exposure_notification)));
            intent.setPackage("com.android.vending");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            activity.finish();
        });

        //noinspection ConstantConditions
        findViewById(R.id.no_btn).setOnClickListener(v -> dismiss());
    }
}
