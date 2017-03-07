package com.jifalops.localization;

import android.os.Bundle;

/**
 * MainActivity is the starting point when using the app. Its main purpose is to allow the user
 * to enter various areas of the app.
 */
public class MainActivity extends AbsLinearLayoutActivity {
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
    }
}