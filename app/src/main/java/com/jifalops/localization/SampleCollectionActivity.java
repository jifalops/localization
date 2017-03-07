package com.jifalops.localization;

import android.os.Bundle;

public class SampleCollectionActivity extends AbsLinearLayoutActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addToLayout(
            "Signal Strength Sampling",
            "2.4GHz & 5GHz WiFi, Bluetooth, and Bluetooth LE",
            RssSamplingActivity.class);

        addToLayout(
            "Time of Flight Sampling",
            "Using Bluetooth and Bluetooth HCI/snoop",
            TofSamplingActivity.class);
    }
}