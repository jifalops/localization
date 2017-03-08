package com.jifalops.localization;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.jifalops.localization.datatypes.Rssi;
import com.jifalops.localization.util.SimpleLog;

import java.util.ArrayList;
import java.util.List;

public class RssSamplingActivity extends AbsActivity {
    static final String TAG = RssSamplingActivity.class.getSimpleName();
    static final int REQUEST_BT_ENABLE = 1;
    static final int REQUEST_BT_DISCOVERABLE = 2;
    static final String SAMPLER = RssSamplingActivity.class.getName() + ".sampler";

    TextView eventLogView, deviceLogView,
            btRssiCountView,
            btleRssiCountView,
            wifiRssiCountView,
            wifi5gRssiCountView,
            deviceIdView;
    EditText distanceView;
    Switch collectSwitch;
    CheckBox btCheckBox, btleCheckBox, wifiCheckBox, wifi5gCheckBox;

    SharedPreferences prefs;
    int logLevel = RssSamplingHelper.LOG_INFORMATIVE;

    ServiceThreadApplication.LocalService service;
    RssSamplingHelper rssSamplingHelper;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rsssampling);
        eventLogView = (TextView) findViewById(R.id.eventLog);
        deviceLogView = (TextView) findViewById(R.id.deviceLog);
        btRssiCountView = (TextView) findViewById(R.id.btRssiCount);
        btleRssiCountView = (TextView) findViewById(R.id.btleRssiCount);
        wifiRssiCountView = (TextView) findViewById(R.id.wifiRssiCount);
        wifi5gRssiCountView = (TextView) findViewById(R.id.wifi5gRssiCount);
        collectSwitch = (Switch) findViewById(R.id.collectSwitch);
        btCheckBox = (CheckBox) findViewById(R.id.btCheckBox);
        btleCheckBox = (CheckBox) findViewById(R.id.btleCheckBox);
        wifiCheckBox = (CheckBox) findViewById(R.id.wifiCheckBox);
        wifi5gCheckBox = (CheckBox) findViewById(R.id.wifi5gCheckBox);

        deviceIdView = (TextView) findViewById(R.id.deviceId);
        distanceView = (EditText) findViewById(R.id.distanceMeters);
        Button sendButton = (Button) findViewById(R.id.sendButton);

        autoScroll((ScrollView) findViewById(R.id.eventScrollView), eventLogView);
        autoScroll((ScrollView) findViewById(R.id.deviceScrollView), deviceLogView);
        deviceIdView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder b = new AlertDialog.Builder(RssSamplingActivity.this);
                final EditText input = new EditText(RssSamplingActivity.this);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                input.setLayoutParams(lp);
//                input.setInputType(InputType.TYPE_CLASS_NUMBER);
                b.setView(input);
                b.setTitle("Device IDs (comma separated)");
                b.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deviceIdView.setText("");
                        List<Integer> deviceIds = new ArrayList<>();
                        rssSamplingHelper.resetKnownDistances();
                        try {
                            String[] ids = input.getText().toString().split(",");
                            int id;
                            for (String s : ids) {
                                id = Integer.valueOf(s);
                                RssSamplingHelper.Device d = rssSamplingHelper.getDevice(id - 1);
                                if (d != null) {
                                    d.isDistanceKnown = true;
                                    deviceIds.add(d.id);
                                }
                            }
                        } catch (Exception ignored) {
                        }
                        boolean first = true;
                        for (int id : deviceIds) {
                            if (first) {
                                deviceIdView.append(id + "");
                                first = false;
                            } else {
                                deviceIdView.append("," + id);
                            }

                        }
                        if (deviceIds.size() == 0) {
                            deviceIdView.setText("0,0,0");
                        }

                    }
                });
                b.show();
            }
        });
        distanceView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    rssSamplingHelper.setDistance(Float.valueOf(s.toString()));
                } catch (NumberFormatException e) {
                    distanceView.setText(rssSamplingHelper.getDistance() + "");
                }
            }
        });
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rssSamplingHelper.send();
            }
        });

        rssSamplingHelper = RssSamplingHelper.getInstance(this);

        prefs = getSharedPreferences(TAG, MODE_PRIVATE);
        logLevel = prefs.getInt("logLevel", logLevel);


        App.getInstance().bindLocalService(this, new ServiceThreadApplication.LocalServiceConnection() {
            @Override
            public void onServiceConnected(ServiceThreadApplication.LocalService service) {
                RssSamplingActivity.this.service = service;
                if (service != null && App.getInstance().isPersistent()) {
                    setupControls();
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName className) {

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!App.getInstance().isPersistent()) {
            rssSamplingHelper.setCollectEnabled(false);
            rssSamplingHelper.close();
            rssSamplingHelper = null;
        }
        App.getInstance().unbindLocalService(null);
    }

    void autoScroll(final ScrollView sv, final TextView tv) {
        tv.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                sv.post(new Runnable() {
                    @Override
                    public void run() {
                        sv.fullScroll(View.FOCUS_DOWN);
                    }
                });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_BT_ENABLE) {
            if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(this, "Activity cannot work unless Bluetooth is enabled.",
                        Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == REQUEST_BT_DISCOVERABLE) {
            if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(this, "Activity cannot work unless device is discoverable.",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_samplecollection, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
//        SubMenu sub = menu.getItem(0);
        menu.findItem(R.id.action_persist).setChecked(App.getInstance().isPersistent());
        menu.findItem(R.id.logImportant).setChecked(
                logLevel == RssSamplingHelper.LOG_IMPORTANT);
        menu.findItem(R.id.logInformative).setChecked(
                logLevel == RssSamplingHelper.LOG_INFORMATIVE);
        menu.findItem(R.id.logAll).setChecked(
                logLevel == RssSamplingHelper.LOG_ALL);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
            case R.id.action_persist:
                if (App.getInstance().isPersistent()) {
                    item.setChecked(false);
                    setPersistent(false);
                } else {
                    item.setChecked(true);
                    setPersistent(true);
                }
                return true;
            case R.id.action_clearSend:
                new AlertDialog.Builder(this)
                        .setMessage("Clear send queue?")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                rssSamplingHelper.clearPendingSendLists();
                                updateSendCounts();
                            }
                        }).show();
                return true;
            case R.id.logImportant:
                logLevel = RssSamplingHelper.LOG_IMPORTANT;
                return true;
            case R.id.logInformative:
                logLevel = RssSamplingHelper.LOG_INFORMATIVE;
                return true;
            case R.id.logAll:
                logLevel = RssSamplingHelper.LOG_ALL;
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    void setPersistent(boolean persist) {
        App.getInstance().setPersistent(persist, getClass());
    }

    void updateSendCounts() {
        updateCountView(App.SIGNAL_BT);
        updateCountView(App.SIGNAL_BTLE);
        updateCountView(App.SIGNAL_WIFI);
        updateCountView(App.SIGNAL_WIFI5G);
    }

    void loadDevicesAndEvents() {
        deviceLogView.setText("Devices:\n");
        eventLogView.setText("Events:\n");
        for (RssSamplingHelper.Device d : rssSamplingHelper.getDevices()) {
            deviceLogView.append(d.toString() + "\n");
        }
        List<SimpleLog.LogItem> items =  rssSamplingHelper.getLog().getBypriority(logLevel, true);
        for (SimpleLog.LogItem item : items) {
            eventLogView.append(item.msg + "\n");
        }
    }

    void setupControls() {
        rssSamplingHelper.setShouldUseBt(prefs.getBoolean("btEnabled", true));
        rssSamplingHelper.setShouldUseBtle(prefs.getBoolean("btleEnabled", true));
        rssSamplingHelper.setShouldUseWifi(prefs.getBoolean("wifiEnabled", true));
        rssSamplingHelper.setShouldUseWifi5g(prefs.getBoolean("wifi5gEnabled", true));

        btCheckBox.setOnCheckedChangeListener(null);
        btCheckBox.setChecked(rssSamplingHelper.getShouldUseBt());
        btCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                rssSamplingHelper.setShouldUseBt(isChecked);
            }
        });
        btleCheckBox.setOnCheckedChangeListener(null);
        btleCheckBox.setChecked(rssSamplingHelper.getShouldUseBtle());
        btleCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                rssSamplingHelper.setShouldUseBtle(isChecked);
            }
        });
        wifiCheckBox.setOnCheckedChangeListener(null);
        wifiCheckBox.setChecked(rssSamplingHelper.getShouldUseWifi());
        wifiCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                rssSamplingHelper.setShouldUseWifi(isChecked);
            }
        });
        wifi5gCheckBox.setOnCheckedChangeListener(null);
        wifi5gCheckBox.setChecked(rssSamplingHelper.getShouldUseWifi5g());
        wifi5gCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                rssSamplingHelper.setShouldUseWifi5g(isChecked);
            }
        });

        collectSwitch.setOnCheckedChangeListener(null);
        collectSwitch.setChecked(rssSamplingHelper.getCollectEnabled());
        collectSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                rssSamplingHelper.setCollectEnabled(isChecked);
            }
        });

        distanceView.setText(rssSamplingHelper.getDistance() + "");
        List<Integer> ids = new ArrayList<>();
        for (RssSamplingHelper.Device d : rssSamplingHelper.getDevices()) {
//            if (d.isDistanceKnown) ids.add(d.id);
        }
        if (ids.size() > 0) deviceIdView.setText(TextUtils.join(",", ids));
    }

    @Override
    protected void onResume() {
        super.onResume();
        rssSamplingHelper.registerListener(samplerListener);
        updateSendCounts();
        loadDevicesAndEvents();
        setupControls();
    }

    @Override
    protected void onPause() {
        super.onPause();
        prefs.edit()
                .putInt("logLevel", logLevel)
                .putBoolean("btEnabled", rssSamplingHelper.getShouldUseBt())
                .putBoolean("btleEnabled", rssSamplingHelper.getShouldUseBtle())
                .putBoolean("wifiEnabled", rssSamplingHelper.getShouldUseWifi())
                .putBoolean("wifi5gEnabled", rssSamplingHelper.getShouldUseWifi5g()).apply();
        rssSamplingHelper.unregisterListener(samplerListener);
    }

    private final RssSamplingHelper.SamplerListener samplerListener = new RssSamplingHelper.SamplerListener() {
        @Override
        public void onMessageLogged(int level, final String msg) {
            if (level >= logLevel) {
                eventLogView.post(new Runnable() {
                    @Override
                    public void run() {
                        eventLogView.append(msg + "\n");
                    }
                });
            }
        }

        @Override
        public void onDeviceFound(RssSamplingHelper.Device device) {
            deviceLogView.append(device.toString() + "\n");
        }

        @Override
        public void onRecordAdded(String signal, RssSamplingHelper.Device device, Rssi r) {
            updateCountView(signal);
        }

        @Override
        public void onSentSuccess(String signal, int count) {
            updateCountView(signal);
        }

        @Override
        public void onSentFailure(String signal, int count, int respCode, String resp, String result) {
            updateCountView(signal);
        }
    };

    void updateCountView(String signal) {
        int count = rssSamplingHelper.getCount(signal);
        TextView tv = null;
        switch (signal) {
            case App.SIGNAL_BT:     tv = btRssiCountView; break;
            case App.SIGNAL_BTLE:   tv = btleRssiCountView; break;
            case App.SIGNAL_WIFI:   tv = wifiRssiCountView; break;
            case App.SIGNAL_WIFI5G: tv = wifi5gRssiCountView; break;
        }
        if (tv != null) tv.setText(count+"");
    }
}