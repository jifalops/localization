package com.jifalops.localization;

import android.os.Bundle;

public class RangingActivity extends AbsLinearLayoutActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addToLayout(
            "Signal Strength Ranging",
            "Test WiFi and Bluetooth RSS estimates",
            RssRangingActivity.class);

        addToLayout(
            "Time of Flight Ranging",
            "Test Bluetooth TOF estimates",
            SampleCollectionActivity.class);
    }
}