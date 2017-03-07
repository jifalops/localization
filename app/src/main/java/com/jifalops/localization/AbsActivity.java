package com.jifalops.localization;

import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;

/**
 * Parent class to Activities in the app.
 */
public abstract class AbsActivity extends Activity {
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}