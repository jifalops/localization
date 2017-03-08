package com.jifalops.localization;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.ScanResult;
import android.content.Context;

import com.jifalops.localization.bluetooth.BtBeacon;
import com.jifalops.localization.bluetooth.BtleBeacon;
import com.jifalops.localization.datatypes.Rssi;
import com.jifalops.localization.util.SimpleLog;
import com.jifalops.localization.wifi.WifiScanner;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class RssSamplingHelper {
    public static final int LOG_IMPORTANT = 3;
    public static final int LOG_INFORMATIVE = 2;
    public static final int LOG_ALL = 1;

    public static class Device {
        public final int id;
        public final String mac, desc;
        public float distance;
        public boolean isDistanceKnown = false;
        public Device(int id, String mac, String desc) {
            this.id = id;
            this.mac = mac;
            this.desc = desc;
        }
        @Override
        public String toString() {
            return id + ": " + desc + " " + mac;
        }
    }


    final List<Device> devices = new ArrayList<>();

    final SimpleLog log = new SimpleLog();

    BtBeacon btBeacon;
    BtleBeacon btleBeacon;
    WifiScanner wifiScanner;

    boolean shouldUseBt = false, shouldUseBtle = false, shouldUseWifi = false, shouldUseWifi5g = false;
    boolean collectEnabled = false;

    float distance = 0;

    private static RssSamplingHelper instance;
    public static RssSamplingHelper getInstance(Context ctx) {
        if (instance == null) instance = new RssSamplingHelper(ctx.getApplicationContext());
        return instance;
    }
    private RssSamplingHelper(Context ctx) {
        btBeacon = BtBeacon.getInstance(ctx);
        btleBeacon = BtleBeacon.getInstance(ctx);
        wifiScanner = new WifiScanner(ctx);

//        rssiHelper = App.getRssiHelper();
//        rssiLoaded = true;


    }

//    void checkIfLoadComplete() {
//        if (rssiLoaded && windowsLoaded && samplesLoaded) {
//            for (SamplerListener l : listeners)
//                l.onDataLoadedFromDisk(rssiHelper, windowHelper, sampleHelper);
//        }
//    }

    public SimpleLog getLog() {
        return log;
    }

    public void setDistance(float d) {
        distance = d;
    }

    public double getDistance() {
        return distance;
    }

    public void close() {
        instance = null;
    }

    public void clearPendingSendLists() {
        App.getInstance().rssWifi4gList.clear();
        App.getInstance().rssWifi5gList.clear();
        App.getInstance().rssBtList.clear();
        App.getInstance().rssBtleList.clear();
    }
    

    public void send() {
//        send(App.SIGNAL_BT, rssiHelper.getBt(), windowHelper.getBt(),
//                rssiHelper.btRW, windowHelper.btRW);
//        send(App.SIGNAL_BTLE, rssiHelper.getBtle(), windowHelper.getBtle(),
//                rssiHelper.btleRW, windowHelper.btleRW);
//        send(App.SIGNAL_WIFI, rssiHelper.getWifi(), windowHelper.getWifi(),
//                rssiHelper.wifiRW, windowHelper.wifiRW);
//        send(App.SIGNAL_WIFI5G, rssiHelper.getWifi5g(), windowHelper.getWifi5g(),
//                rssiHelper.wifi5gRW, windowHelper.wifi5gRW);
//        addEvent(LOG_IMPORTANT, "RSSI and Windows sent.");
    }
    
    public List<Device> getDevices() {
        return devices;
    }

    public Device getDevice(int index) {
        return devices.get(index);
    }

    public void resetKnownDistances() {
        for (Device d : devices) {
            d.isDistanceKnown = false;
        }
    }

    private Device getDevice(String mac, String desc) {
        Device device = null;
        for (Device d : devices) {
            if (d.mac.equals(mac)) {
                device = d;
                break;
            }
        }
        if (device == null) {
            device = new Device(devices.size()+1, mac, desc);
            devices.add(device);
            addEvent(LOG_INFORMATIVE, "Found new device, " + device.id);
            for (SamplerListener l : listeners) l.onDeviceFound(device);
        }
        return device;
    }

    private void enableBt() {
        btBeacon.registerListener(btBeaconListener);
        btBeacon.startBeaconing();
    }
    private void disableBt() {
        btBeacon.stopBeaconing();
        btBeacon.unregisterListener(btBeaconListener);
    }
    private void enableBtle() {
        btleBeacon.registerListener(btleBeaconListener);
        btleBeacon.startBeaconing();
    }
    private void disableBtle() {
        btleBeacon.stopBeaconing();
        btleBeacon.unregisterListener(btleBeaconListener);
    }
    private void enableWifi() {
        wifiScanner.registerListener(wifiScanListener);
        wifiScanner.startScanning(100);
    }
    private void disableWifi() {
        wifiScanner.stopScanning();
        wifiScanner.unregisterListener(wifiScanListener);
    }

    public void setShouldUseBt(boolean use) {
        if (shouldUseBt == use) return;
        shouldUseBt = use;
        if (use && collectEnabled) enableBt();
        else if (!use && collectEnabled) disableBt();
    }
    public void setShouldUseBtle(boolean use) {
        if (shouldUseBtle == use) return;
        shouldUseBtle = use;
        if (use && collectEnabled) enableBtle();
        else if (!use && collectEnabled) disableBtle();
    }
    public void setShouldUseWifi(boolean use) {
        if (shouldUseWifi == use) return;
        shouldUseWifi = use;
        if (use && collectEnabled && !shouldUseWifi5g) enableWifi();
        else if (!use && collectEnabled && !shouldUseWifi5g) disableWifi();
    }

    public void setShouldUseWifi5g(boolean use) {
        if (shouldUseWifi5g == use) return;
        shouldUseWifi5g = use;
        if (use && collectEnabled && !shouldUseWifi) enableWifi();
        else if (!use && collectEnabled && !shouldUseWifi) disableWifi();
    }

    public void setCollectEnabled(boolean enabled) {
        if (collectEnabled == enabled) return;
        collectEnabled = enabled;
        if (enabled) {
            if (shouldUseBt) enableBt();
            if (shouldUseBtle) enableBtle();
            if (shouldUseWifi || shouldUseWifi5g) enableWifi();
        } else {
            if (shouldUseBt) disableBt();
            if (shouldUseBtle) disableBtle();
            if (shouldUseWifi || shouldUseWifi5g) disableWifi();
        }
    }

    public boolean getShouldUseBt() { return shouldUseBt; }
    public boolean getShouldUseBtle() { return shouldUseBtle; }
    public boolean getShouldUseWifi() { return shouldUseWifi; }
    public boolean getShouldUseWifi5g() { return shouldUseWifi5g; }
    public boolean getCollectEnabled() { return collectEnabled; }

    void addEvent(int level, String msg) {
        log.add(level, msg);
        for (SamplerListener l : listeners) l.onMessageLogged(level, msg);
    }

    void addRecord(Device device, String signal, int rssi, int freq) {
        if (collectEnabled) {
            if (device.isDistanceKnown && rssi <= 0 && distance > 0) {
                addEvent(LOG_INFORMATIVE, "Device " + device.id + ": " +
                        rssi + " dBm (" + freq + " MHz) at " +
                        distance + "m (" + signal + ").");
//                String time = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.US).format(new Date());
                Rssi record = null;
                switch (signal) {
                    case App.SIGNAL_BT:
                         record = new Rssi(App.getInstance().btMac, device.mac, rssi, freq,
                            0, distance);
                        App.getInstance().rssBtList.add(record.toString());
                        break;
                    case App.SIGNAL_BTLE:
                        record = new Rssi(App.getInstance().btMac, device.mac, rssi, freq,
                                0, distance);
                        App.getInstance().rssBtleList.add(record.toString());
                        break;
                    case App.SIGNAL_WIFI:
                        record = new Rssi(App.getInstance().wifiMac, device.mac, rssi, freq,
                                0, distance);
                        App.getInstance().rssWifi4gList.add(record.toString());
                        break;
                    case App.SIGNAL_WIFI5G:
                        record = new Rssi(App.getInstance().wifiMac, device.mac, rssi, freq,
                                0, distance);
                        App.getInstance().rssWifi5gList.add(record.toString());
                        break;
                }
                for (SamplerListener l : listeners) l.onRecordAdded(signal, device, record);
            } else {
                addEvent(LOG_ALL, "Ignoring " + rssi + " dBm (" + freq + " MHz) for device " +
                        device.id + " (" + signal + ").");
            }
        }
    }

    public int getCount(String signal) {
        switch (signal) {
            case App.SIGNAL_BT:     return App.getInstance().rssBtList.size();
            case App.SIGNAL_BTLE:   return App.getInstance().rssBtleList.size();
            case App.SIGNAL_WIFI:   return App.getInstance().rssWifi4gList.size();
            case App.SIGNAL_WIFI5G: return App.getInstance().rssWifi5gList.size();
        }
        return 0;
    }

    final BtBeacon.BtBeaconListener btBeaconListener = new BtBeacon.BtBeaconListener() {
        @Override
        public void onDeviceFound(BluetoothDevice device, short rssi) {
            addRecord(getDevice(device.getAddress(), device.getName() + " (BT)"),
                    App.SIGNAL_BT, rssi, 2400);
        }

        @Override
        public void onThisDeviceDiscoverableChanged(boolean discoverable) {
            addEvent(LOG_INFORMATIVE, "BT Discoverability changed to " + discoverable);
        }

        @Override
        public void onDiscoveryStarting() {
            addEvent(LOG_ALL, "Scanning for BT devices...");
        }
    };

    final BtleBeacon.BtleBeaconListener btleBeaconListener = new BtleBeacon.BtleBeaconListener() {
        @Override
        public void onAdvertiseNotSupported() {
            addEvent(LOG_IMPORTANT, "BTLE advertisement not supported on this device.");
        }

        @Override
        public void onAdvertiseStartSuccess(AdvertiseSettings settingsInEffect) {
            addEvent(LOG_IMPORTANT, "BTLE advertising started at " +
                    settingsInEffect.getTxPowerLevel() + " dBm.");
        }

        @Override
        public void onAdvertiseStartFailure(int errorCode, String errorMsg) {
            addEvent(LOG_IMPORTANT, "BTLE advertisements failed to start (" + errorCode + "): " + errorMsg) ;
        }

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            handleScanResult(result);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            addEvent(LOG_ALL, "Received " + results.size() + " batch scan results (BTLE).");
            for (ScanResult sr : results) {
                handleScanResult(sr);
            }
        }

        @Override
        public void onScanFailed(int errorCode, String errorMsg) {
            addEvent(LOG_IMPORTANT, "BT-LE scan failed (" + errorCode + "): " + errorMsg);
        }

        void handleScanResult(ScanResult result) {
            BluetoothDevice device = result.getDevice();
            if (device != null) {
                addRecord(getDevice(device.getAddress(), device.getName() + " (BTLE)"),
                        App.SIGNAL_BTLE, result.getRssi(), 2400);
            } else {
                addEvent(LOG_INFORMATIVE, "BTLE received " + result.getRssi() + " dBm from null device.");
            }
        }
    };

    final WifiScanner.ScanListener wifiScanListener = new WifiScanner.ScanListener() {
        @Override
        public void onScanResults(List<android.net.wifi.ScanResult> scanResults) {
            addEvent(LOG_ALL, "WiFi found " + scanResults.size() + " results.");
            for (android.net.wifi.ScanResult r : scanResults) {
                if (r.frequency < 4000 && shouldUseWifi) {
                    addRecord(getDevice(r.BSSID, r.SSID + " (WiFi " + r.frequency + "MHz)"),
                            App.SIGNAL_WIFI, r.level, r.frequency);
                } else if (r.frequency > 4000 && shouldUseWifi5g) {
                    addRecord(getDevice(r.BSSID, r.SSID + " (WiFi " + r.frequency + "MHz)"),
                            App.SIGNAL_WIFI5G, r.level, r.frequency);
                }

            }
        }
    };

    public interface SamplerListener {
        void onMessageLogged(int level, String msg);
        void onDeviceFound(Device device);
        void onRecordAdded(String signal, Device device, Rssi r);
        void onSentSuccess(String signal, int count);
        void onSentFailure(String signal, int count, int respCode, String resp, String result);
    }
    private final List<SamplerListener> listeners = new ArrayList<>(1);
    public boolean registerListener(SamplerListener l) {
        return !listeners.contains(l) && listeners.add(l);
    }
    public boolean unregisterListener(SamplerListener l) {
        return listeners.remove(l);
    }
}
