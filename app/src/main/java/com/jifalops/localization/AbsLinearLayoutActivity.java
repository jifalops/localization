package com.jifalops.localization;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Dynamic list of links to other Activities.
 */
public abstract class AbsLinearLayoutActivity extends AbsActivity {
    private LinearLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_linearlayout);
        layout = (LinearLayout) findViewById(R.id.linearLayout);
    }

    protected void addToLayout(String text, final Class<?> clazz) {
        View v = getLayoutInflater().inflate(android.R.layout.simple_list_item_activated_1, layout, false);
        ((TextView) v.findViewById(android.R.id.text1)).setText(text);
        if (clazz != null) {
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(AbsLinearLayoutActivity.this, clazz));
                }
            });
        }
        layout.addView(v);
    }

    protected void addToLayout(String line1, String line2, final Class<?> clazz) {
        View v = getLayoutInflater().inflate(android.R.layout.simple_list_item_activated_2, layout, false);
        ((TextView) v.findViewById(android.R.id.text1)).setText(line1);
        ((TextView) v.findViewById(android.R.id.text2)).setText(line2);
        if (clazz != null) {
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(AbsLinearLayoutActivity.this, clazz));
                }
            });
        }
        layout.addView(v);
    }
}