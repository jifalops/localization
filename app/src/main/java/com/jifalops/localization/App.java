package com.jifalops.localization;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.jifalops.localization.bluetooth.BtHelper;
import com.jifalops.localization.datatypes.RangingParams;
import com.jifalops.localization.datatypes.RefiningParams;
import com.jifalops.localization.util.FileBackedArrayList;
import com.jifalops.localization.wifi.WifiHelper;

import java.io.File;


/**
 * App is the central area for managing global application state.
 */
public class App extends ServiceThreadApplication {
    private static final String TAG = App.class.getSimpleName();

    public static final String NSD_SERVICE_PREFIX       = "localiz_";
    public static final String WIFI_BEACON_SSID_PREFIX  = "localiz_";

    public static final String SIGNAL_BT                = "bt";
    public static final String SIGNAL_BTLE              = "btle";
    public static final String SIGNAL_WIFI              = "wifi";
    public static final String SIGNAL_WIFI5G            = "wifi5g";

    // Raw sample caches
    public static final String FILE_RSS_WIFI4G_SAMPLES  = "rss_wifi4g_samples.csv";
    public static final String FILE_RSS_WIFI5G_SAMPLES  = "rss_wifi5g_samples.csv";
    public static final String FILE_RSS_BT_SAMPLES      = "rss_bt_samples.csv";
    public static final String FILE_RSS_BTLE_SAMPLES    = "rss_btle_samples.csv";
    public static final String FILE_TOF_BT_HCI_SAMPLES  = "tof_bt_hci_samples.csv";
    public static final String FILE_TOF_BT_JAVA_SAMPLES = "tof_bt_java_samples.csv";

    // Ranging estimate caches
    public static final String FILE_RSS_WIFI4G_RANGING  = "rss_wifi4g_ranging.csv";
    public static final String FILE_RSS_WIFI5G_RANGING  = "rss_wifi5g_ranging.csv";
    public static final String FILE_RSS_BT_RANGING      = "rss_bt_ranging.csv";
    public static final String FILE_RSS_BTLE_RANGING    = "rss_btle_ranging.csv";
    public static final String FILE_TOF_BT_HCI_RANGING  = "tof_bt_hci_ranging.csv";
    public static final String FILE_TOF_BT_JAVA_RANGING = "tof_bt_java_ranging.csv";

    private static App instance;
    public static App getInstance() {
        return instance;
    }

    private SharedPreferences prefs;

    // Samples cache
    public FileBackedArrayList rssWifi4gSamples;
    public FileBackedArrayList rssWifi5gSamples;
    public FileBackedArrayList rssBtSamples;
    public FileBackedArrayList rssBtleSamples;
    public FileBackedArrayList tofBtHciSamples;
    public FileBackedArrayList tofBtJavaSamples;

    // Ranging cache
    public FileBackedArrayList rssWifi4gRanging;
    public FileBackedArrayList rssWifi5gRanging;
    public FileBackedArrayList rssBtRanging;
    public FileBackedArrayList rssBtleRanging;
    public FileBackedArrayList tofBtHciRanging;
    public FileBackedArrayList tofBtJavaRanging;
    
    public RefiningParams rssWifi4gRefiningParams;
    public RefiningParams rssWifi5gRefiningParams;
    public RefiningParams rssBtRefiningParams;
    public RefiningParams rssBtleRefiningParams;
    public RefiningParams tofBtHciRefiningParams;
    public RefiningParams tofBtJavaRefiningParams;

    public RangingParams rssWifi4gRangingParams;
    public RangingParams rssWifi5gRangingParams;
    public RangingParams rssBtRangingParams;
    public RangingParams rssBtleRangingParams;
    public RangingParams tofBtHciRangingParams;
    public RangingParams tofBtJavaRangingParams;

    public String wifiMac, btMac;
    public FirebaseUser firebaseUser;
    public DatabaseReference database;
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener;


    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        initWifiMac();
        initBtMac();

        auth = FirebaseAuth.getInstance();
        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                firebaseUser = firebaseAuth.getCurrentUser();
                if (firebaseUser != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + firebaseUser.getUid());
                    fetchParameters();
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };

        database = FirebaseDatabase.getInstance().getReference();

        bindLocalService(this, new LocalServiceConnection() {
            @Override
            public void onServiceConnected(LocalService service) {
                loadDataFiles();
                auth.addAuthStateListener(authListener);
                auth.signInAnonymously()
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                Log.d(TAG, "signInAnonymously:onComplete:" + task.isSuccessful());

                                // If sign in fails, display a message to the user. If sign in succeeds
                                // the auth state listener will be notified and logic to handle the
                                // signed in user can be handled in the listener.
                                if (!task.isSuccessful()) {
                                    Log.w(TAG, "signInAnonymously", task.getException());
                                    Toast.makeText(App.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
            @Override public void onServiceDisconnected(ComponentName className) {
                if (authListener != null) {
                    auth.removeAuthStateListener(authListener);
                }
            }
        });
    }

    private void initWifiMac() {
        final WifiHelper wifi = WifiHelper.getInstance(this);
        wifiMac = wifi.getMacAddress();
        if (TextUtils.isEmpty(wifiMac)) {
            registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, -1);
                    if (state == WifiManager.WIFI_STATE_ENABLED) {
                        wifiMac = wifi.getMacAddress();
                        if (TextUtils.isEmpty(wifiMac)) {
                            Log.e(TAG, "WiFi is enabled but no MAC address available");
                        } else {
                            unregisterReceiver(this);
                        }
                    }
                }
            }, new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION));
        }
    }

    private void initBtMac() {
        final BtHelper bt = BtHelper.getInstance(this);
        btMac = bt.getMacAddress();
        if (TextUtils.isEmpty(btMac)) {
            registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
                    if (state == BluetoothAdapter.STATE_ON) {
                        btMac = bt.getMacAddress();
                        if (TextUtils.isEmpty(btMac)) {
                            Log.e(TAG, "BT is enabled but no MAC address available");
                        } else {
                            unregisterReceiver(this);
                        }
                    }
                }
            }, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        }
    }

    private void loadDataFiles() {
        File dir = this.getExternalFilesDir(null);
        
        // Samples cache
        rssWifi4gSamples = new FileBackedArrayList(new File(dir, FILE_RSS_WIFI4G_SAMPLES), null);
        rssWifi5gSamples = new FileBackedArrayList(new File(dir, FILE_RSS_WIFI5G_SAMPLES), null);
        rssBtSamples = new FileBackedArrayList(new File(dir, FILE_RSS_BT_SAMPLES), null);
        rssBtleSamples = new FileBackedArrayList(new File(dir, FILE_RSS_BTLE_SAMPLES), null);
        tofBtHciSamples = new FileBackedArrayList(new File(dir, FILE_TOF_BT_HCI_SAMPLES), null);
        tofBtJavaSamples = new FileBackedArrayList(new File(dir, FILE_TOF_BT_JAVA_SAMPLES), null);

        // Ranging cache
        rssWifi4gRanging = new FileBackedArrayList(new File(dir, FILE_RSS_WIFI4G_RANGING), null);
        rssWifi5gRanging = new FileBackedArrayList(new File(dir, FILE_RSS_WIFI5G_RANGING), null);
        rssBtRanging = new FileBackedArrayList(new File(dir, FILE_RSS_BT_RANGING), null);
        rssBtleRanging = new FileBackedArrayList(new File(dir, FILE_RSS_BTLE_RANGING), null);
        tofBtHciRanging = new FileBackedArrayList(new File(dir, FILE_TOF_BT_HCI_RANGING), null);
        tofBtJavaRanging = new FileBackedArrayList(new File(dir, FILE_TOF_BT_JAVA_RANGING), null);
    }

    private void fetchParameters() {
        if (database == null || firebaseUser == null) return;

    }
}
