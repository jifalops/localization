package com.jifalops.localization;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.content.Context;

import com.jifalops.localization.bluetooth.BtBeacon;
import com.jifalops.localization.bluetooth.BtleBeacon;
import com.jifalops.localization.datatypes.RangingParams;
import com.jifalops.localization.datatypes.RefiningParams;
import com.jifalops.localization.datatypes.Rss;
import com.jifalops.localization.datatypes.RssBtle;
import com.jifalops.localization.datatypes.RssRanging;
import com.jifalops.localization.datatypes.RssWifi;
import com.jifalops.localization.datatypes.Sample;
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
        App.getInstance().rssWifi4gSamples.clear();
        App.getInstance().rssWifi5gSamples.clear();
        App.getInstance().rssBtSamples.clear();
        App.getInstance().rssBtleSamples.clear();
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

    void addBtRecord(Device device, int rssi) {
        if (collectEnabled) {
            if (device.isDistanceKnown && rssi <= 0 && distance > 0) {
                Rss record = new Rss(App.getInstance().btMac, device.mac, rssi, distance);
                float immediateRange = 0, refinedRange = 0;
                App.getInstance().rssBtSamples.add(record.toString());
                if (App.getInstance().rssBtRangingParams != null) {
                    immediateRange = App.getInstance().rssBtRangingParams.estimateRange(record.getInputs());
                    RefiningParams.Sample s = App.getInstance().rssBtRefiningParams.sampler.add(record);
                    if (s != null) {
                        refinedRange = App.getInstance().rssBtRangingParams.estimateRange(s.getInputs());
                        App.getInstance().rssBtRanging.add(new RssRanging(App.getInstance().btMac,
                                device.mac, (float) s.getInputs()[0], distance, refinedRange,
                                RangingParams.freeSpacePathLoss(s.getInputs()[0], 2400)).toString());
                        //
                        // TODO copy the above to the other sections and clean that shit up
                        //

                    }
                }
                String s = "Device " + device.id + " (BT): " + rssi + ". Act: " + distance;
                if (immediateRange > 0) s += ". Imm: " + Math.round(immediateRange * 10) / 10;
                if (refinedRange > 0) s += ". Ref: " + Math.round(immediateRange * 10) / 10;
                addEvent(LOG_INFORMATIVE,  s);
                for (SamplerListener l : listeners) l.onRecordAdded(App.SIGNAL_BT, device, record,
                        immediateRange, refinedRange);
            } else {
                addEvent(LOG_ALL, "Ignoring " + rssi + " dBm from device " +
                        device.id + " (bt).");
            }
        }
    }
    void addBtleRecord(Device device, int rssi, int txPower) {
        if (collectEnabled) {
            if (device.isDistanceKnown && rssi <= 0 && distance > 0) {
                RssBtle record = new RssBtle(App.getInstance().btMac, device.mac, rssi, txPower, distance);
                float immediateRange = 0, refinedRange = 0;
                App.getInstance().rssBtleSamples.add(record.toString());
                if (App.getInstance().rssBtleRangingParams != null) {
                    immediateRange = App.getInstance().rssBtleRangingParams.estimateRange(record.getInputs());
                    refinedRange = App.getInstance().rssBtleRangingParams.sampler.add(record);
                }
                String s = "Device " + device.id + " (LE): " + rssi + ". Act: " + distance;
                if (immediateRange > 0) s += ". Imm: " + Math.round(immediateRange * 10) / 10;
                if (refinedRange > 0) s += ". Ref: " + Math.round(immediateRange * 10) / 10;
                addEvent(LOG_INFORMATIVE,  s);
                for (SamplerListener l : listeners) l.onRecordAdded(App.SIGNAL_BTLE, device, record,
                        immediateRange, refinedRange);
            } else {
                addEvent(LOG_ALL, "Ignoring " + rssi + " dBm from device " +
                        device.id + " (btle).");
            }
        }
    }
    void addWifi4gRecord(Device device, int rssi, int freq, int width) {
        if (collectEnabled) {
            if (device.isDistanceKnown && rssi <= 0 && distance > 0) {
                RssWifi record = new RssWifi(App.getInstance().btMac, device.mac, rssi, freq, width, distance);
                float immediateRange = 0, refinedRange = 0;
                App.getInstance().rssWifi4gSamples.add(record.toString());
                if (App.getInstance().rssWifi4gRangingParams != null) {
                    immediateRange = App.getInstance().rssWifi4gRangingParams.estimateRange(record.getInputs());
                    refinedRange = App.getInstance().rssWifi4gRangingParams.sampler.add(record);
                }
                String s = "Device " + device.id + " (4G): " + rssi + ". Act: " + distance;
                if (immediateRange > 0) s += ". Imm: " + Math.round(immediateRange * 10) / 10;
                if (refinedRange > 0) s += ". Ref: " + Math.round(immediateRange * 10) / 10;
                addEvent(LOG_INFORMATIVE,  s);
                for (SamplerListener l : listeners) l.onRecordAdded(App.SIGNAL_WIFI, device, record,
                        immediateRange, refinedRange);
            } else {
                addEvent(LOG_ALL, "Ignoring " + rssi + " dBm from device " +
                        device.id + " (wifi4g).");
            }
        }
    }
    void addWifi5gRecord(Device device, int rssi, int freq, int width) {
        if (collectEnabled) {
            if (device.isDistanceKnown && rssi <= 0 && distance > 0) {
                RssWifi record = new RssWifi(App.getInstance().btMac, device.mac, rssi, freq, width, distance);
                float immediateRange = 0, refinedRange = 0;
                App.getInstance().rssWifi5gSamples.add(record.toString());
                if (App.getInstance().rssWifi5gRangingParams != null) {
                    immediateRange = App.getInstance().rssWifi5gRangingParams.estimateRange(record.getInputs());
                    refinedRange = App.getInstance().rssWifi5gRangingParams.sampler.add(record);
                }
                String s = "Device " + device.id + " (5G): " + rssi + ". Act: " + distance;
                if (immediateRange > 0) s += ". Imm: " + Math.round(immediateRange * 10) / 10;
                if (refinedRange > 0) s += ". Ref: " + Math.round(immediateRange * 10) / 10;
                addEvent(LOG_INFORMATIVE,  s);
                for (SamplerListener l : listeners) l.onRecordAdded(App.SIGNAL_WIFI5G, device, record,
                        immediateRange, refinedRange);
            } else {
                addEvent(LOG_ALL, "Ignoring " + rssi + " dBm from device " +
                        device.id + " (wifi5g).");
            }
        }
    }

    public int getCount(String signal) {
        switch (signal) {
            case App.SIGNAL_BT:     return App.getInstance().rssBtSamples.size();
            case App.SIGNAL_BTLE:   return App.getInstance().rssBtleSamples.size();
            case App.SIGNAL_WIFI:   return App.getInstance().rssWifi4gSamples.size();
            case App.SIGNAL_WIFI5G: return App.getInstance().rssWifi5gSamples.size();
        }
        return 0;
    }

    final BtBeacon.BtBeaconListener btBeaconListener = new BtBeacon.BtBeaconListener() {
        @Override
        public void onDeviceFound(BluetoothDevice device, short rssi) {
            addBtRecord(getDevice(device.getAddress(), device.getName() + " (BT)"),
                    rssi);
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
                ScanRecord r = result.getScanRecord();
                addBtleRecord(getDevice(device.getAddress(), device.getName() + " (BTLE)"),
                    result.getRssi(), r == null ? Integer.MIN_VALUE : r.getTxPowerLevel());
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
                    addWifi4gRecord(getDevice(r.BSSID, r.SSID + " (WiFi " + r.frequency + "MHz)"),
                            r.level, r.frequency, r.channelWidth);
                } else if (r.frequency > 4000 && shouldUseWifi5g) {
                    addWifi5gRecord(getDevice(r.BSSID, r.SSID + " (WiFi " + r.frequency + "MHz)"),
                            r.level, r.frequency, r.channelWidth);
                }
            }
        }
    };

    public interface SamplerListener {
        void onMessageLogged(int level, String msg);
        void onDeviceFound(Device device);
        void onRecordAdded(String signal, Device device, Sample r,
                           float immediateRange, float refinedRange);
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
