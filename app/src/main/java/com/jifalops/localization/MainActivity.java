package com.jifalops.localization;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

/**
 * MainActivity is the starting point when using the app. Its main purpose is to allow the user
 * to enter various areas of the app.
 */
public class MainActivity extends AbsLinearLayoutActivity {
    private static final int LOCATION_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addToLayout(
            "Sample Collection",
            "Collect raw samples using RSS or TOF",
            SampleCollectionActivity.class);

        addToLayout(
            "Ranging",
            "Ranging estimates in real time",
            RangingActivity.class);

        addToLayout(
            "Localization",
            "Create a coordinate system with other devices",
            LocalizationActivity.class);

        addToLayout(
            "Demos",
            "Test individual building blocks of the app",
            DemoActivity.class);

        requestPermissions();
    }

    private void requestPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            Toast.makeText(this, "WiFi Scanning requires Coarse Location permission",
                    Toast.LENGTH_LONG).show();


            App.getInstance().postDelayed(new Runnable() {
                @Override
                public void run() {
                    ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        LOCATION_PERMISSION);
                }
            }, 3000);

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                   String permissions[], int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {

                }
            }
        }
    }
}