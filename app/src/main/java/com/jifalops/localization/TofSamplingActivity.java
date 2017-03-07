package com.jifalops.localization;

import android.os.Bundle;


public class TofSamplingActivity extends AbsLinearLayoutActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addToLayout(
            "Here be dragons",
            "Click to see if the app crashes",
            null);
    }
}