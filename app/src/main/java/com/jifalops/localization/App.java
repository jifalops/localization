package com.jifalops.localization;

import android.content.ComponentName;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.Settings;

import com.jifalops.localization.datatypes.NnSettings;
import com.jifalops.localization.util.FileBackedArrayList;

import java.io.File;


/**
 * App is the central area for managing global application state.
 */
public class App extends ServiceThreadApplication {
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

    public static final String DEVICE_ID = Settings.Secure.ANDROID_ID;

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

    public NnSettings rssWifi4gSettings;
    public NnSettings rssWifi5gSettings;
    public NnSettings rssBtSettings;
    public NnSettings rssBtleSettings;
    public NnSettings tofBtSettings;
    public NnSettings tofBtleSettings;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        bindLocalService(this, new LocalServiceConnection() {
            @Override
            public void onServiceConnected(LocalService service) {
                loadDataFiles();
            }
            @Override public void onServiceDisconnected(ComponentName className) {}
        });
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
                rssWifi4gSettings = new NnSettings(nnRssWifi4gList.get(0).split(","));
            }
        });
        nnRssWifi5gList = new FileBackedArrayList(new File(dir, FILE_NN_RSS_WIFI5G), new Runnable() {
            @Override
            public void run() {
                rssWifi5gSettings = new NnSettings(nnRssWifi5gList.get(0).split(","));
            }
        });
        nnRssBtList = new FileBackedArrayList(new File(dir, FILE_NN_RSS_BT), new Runnable() {
            @Override
            public void run() {
                rssBtSettings = new NnSettings(nnRssBtList.get(0).split(","));
            }
        });
        nnRssBtleList = new FileBackedArrayList(new File(dir, FILE_NN_RSS_BTLE), new Runnable() {
            @Override
            public void run() {
                rssBtleSettings = new NnSettings(nnRssBtleList.get(0).split(","));
            }
        });
        nnTofBtHciList = new FileBackedArrayList(new File(dir, FILE_NN_TOF_BT_HCI), new Runnable() {
            @Override
            public void run() {
                tofBtSettings = new NnSettings(nnTofBtHciList.get(0).split(","));
            }
        });
        nnTofBtJavaList = new FileBackedArrayList(new File(dir, FILE_NN_TOF_BT_JAVA), new Runnable() {
            @Override
            public void run() {
                tofBtleSettings = new NnSettings(nnTofBtJavaList.get(0).split(","));
            }
        });
    }
}
