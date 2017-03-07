package com.jifalops.localization;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;


/**
 * App is the central area for managing global application state.
 */
public class App extends ServiceThreadApplication {
    public static final String NSD_SERVICE_PREFIX = "localiz_";
    public static final String WIFI_BEACON_SSID_PREFIX = "localiz_";
    public static final String SIGNAL_BT = "bt";
    public static final String SIGNAL_BTLE = "btle";
    public static final String SIGNAL_WIFI = "wifi";
    public static final String SIGNAL_WIFI5G = "wifi5g";
    public static final String DATA_RSSI = "rssi";
    public static final String DATA_SAMPLES = "samples";
    public static final String DATA_ESTIMATORS = "estimators";

    private static App instance;
    public static App getInstance() {
        return instance;
    }

    private SharedPreferences prefs;
    private String wifiMac, btMac;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        // TODO load files in background.
        // TODO lazy load Wifi and BT mac addresses when they are turned on.
    }

    public static String getWifiMac() {
        return instance.wifiMac;
    }
    public static String getBtMac() {
        return instance.btMac;
    }

    public static void broadcast(String intentAction) {
        LocalBroadcastManager.getInstance(instance).sendBroadcast(
                new Intent(intentAction));
    }
}
