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
import com.jifalops.localization.util.FileBackedArrayList;
import com.jifalops.localization.wifi.WifiHelper;

import java.io.File;


/**
 * App is the central area for managing global application state.
 */
public class App extends ServiceThreadApplication {
    private static final String TAG = App.class.getSimpleName();

    public static final String NSD_SERVICE_PREFIX      = "localiz_";
    public static final String WIFI_BEACON_SSID_PREFIX = "localiz_";

    public static final String SIGNAL_BT           = "bt";
    public static final String SIGNAL_BTLE         = "btle";
    public static final String SIGNAL_WIFI         = "wifi";
    public static final String SIGNAL_WIFI5G       = "wifi5g";

    public static final String DATA_RSSI           = "rssi";
    public static final String DATA_SAMPLES        = "samples";
    public static final String DATA_ESTIMATORS     = "estimators";

    public static final String FILE_RSS_WIFI4G     = "rss_wifi4g.csv";
    public static final String FILE_RSS_WIFI5G     = "rss_wifi5g.csv";
    public static final String FILE_RSS_BT         = "rss_bt.csv";
    public static final String FILE_RSS_BTLE       = "rss_btle.csv";
    public static final String FILE_TOF_BT_HCI     = "tof_bt_hci.csv";
    public static final String FILE_TOF_BT_JAVA    = "tof_bt_java.csv";

    public static final String FILE_NN_RSS_WIFI4G  = "nn_rss_wifi4g.csv";
    public static final String FILE_NN_RSS_WIFI5G  = "nn_rss_wifi5g.csv";
    public static final String FILE_NN_RSS_BT      = "nn_rss_bt.csv";
    public static final String FILE_NN_RSS_BTLE    = "nn_rss_btle.csv";
    public static final String FILE_NN_TOF_BT_HCI  = "nn_tof_bt_hci.csv";
    public static final String FILE_NN_TOF_BT_JAVA = "nn_tof_bt_java.csv";

    private static App instance;
    public static App getInstance() {
        return instance;
    }

    private SharedPreferences prefs;

    public FileBackedArrayList rssWifi4gList;
    public FileBackedArrayList rssWifi5gList;
    public FileBackedArrayList rssBtList;
    public FileBackedArrayList rssBtleList;
    public FileBackedArrayList tofBtHciList;
    public FileBackedArrayList tofBtJavaList;

    // These files are only one line.
    public FileBackedArrayList nnRssWifi4gList;
    public FileBackedArrayList nnRssWifi5gList;
    public FileBackedArrayList nnRssBtList;
    public FileBackedArrayList nnRssBtleList;
    public FileBackedArrayList nnTofBtHciList;
    public FileBackedArrayList nnTofBtJavaList;

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
        rssWifi4gList = new FileBackedArrayList(new File(dir, FILE_RSS_WIFI4G), null);
        rssWifi5gList = new FileBackedArrayList(new File(dir, FILE_RSS_WIFI5G), null);
        rssBtList = new FileBackedArrayList(new File(dir, FILE_RSS_BT), null);
        rssBtleList = new FileBackedArrayList(new File(dir, FILE_RSS_BTLE), null);
        tofBtHciList = new FileBackedArrayList(new File(dir, FILE_TOF_BT_HCI), null);
        tofBtJavaList = new FileBackedArrayList(new File(dir, FILE_TOF_BT_JAVA), null);

        // These files are only one line.
        nnRssWifi4gList = new FileBackedArrayList(new File(dir, FILE_NN_RSS_WIFI4G), new Runnable() {
            @Override
            public void run() {
                rssWifi4gRangingParams = new RangingParams(nnRssWifi4gList.get(0).split(","));
            }
        });
        nnRssWifi5gList = new FileBackedArrayList(new File(dir, FILE_NN_RSS_WIFI5G), new Runnable() {
            @Override
            public void run() {
                rssWifi5gRangingParams = new RangingParams(nnRssWifi5gList.get(0).split(","));
            }
        });
        nnRssBtList = new FileBackedArrayList(new File(dir, FILE_NN_RSS_BT), new Runnable() {
            @Override
            public void run() {
                rssBtRangingParams = new RangingParams(nnRssBtList.get(0).split(","));
            }
        });
        nnRssBtleList = new FileBackedArrayList(new File(dir, FILE_NN_RSS_BTLE), new Runnable() {
            @Override
            public void run() {
                rssBtleRangingParams = new RangingParams(nnRssBtleList.get(0).split(","));
            }
        });
        nnTofBtHciList = new FileBackedArrayList(new File(dir, FILE_NN_TOF_BT_HCI), new Runnable() {
            @Override
            public void run() {
                tofBtHciRangingParams = new RangingParams(nnTofBtHciList.get(0).split(","));
            }
        });
        nnTofBtJavaList = new FileBackedArrayList(new File(dir, FILE_NN_TOF_BT_JAVA), new Runnable() {
            @Override
            public void run() {
                tofBtJavaRangingParams = new RangingParams(nnTofBtJavaList.get(0).split(","));
            }
        });
    }
}
