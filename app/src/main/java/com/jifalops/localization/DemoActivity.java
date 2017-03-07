package com.jifalops.localization;

import android.os.Bundle;

import com.jifalops.localization.bluetooth.BtBeaconDemoActivity;
import com.jifalops.localization.bluetooth.BtleBeaconDemoActivity;
import com.jifalops.localization.wifi.WifiScannerDemoActivity;

public class DemoActivity extends AbsLinearLayoutActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addToLayout(
            "WiFi Scanner",
            WifiScannerDemoActivity.class);

        addToLayout(
            "Bluetooth Beaconing",
            BtBeaconDemoActivity.class);

        addToLayout(
            "Bluetooth LE Beaconing",
            BtleBeaconDemoActivity.class);
    }
}