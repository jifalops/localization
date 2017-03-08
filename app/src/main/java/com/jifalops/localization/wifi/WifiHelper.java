package com.jifalops.localization.wifi;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.util.Collections;
import java.util.List;

/**
 *
 */
public final class WifiHelper {
    private static final String TAG = WifiHelper.class.getSimpleName();

    private WifiManager wifiManager;
    private ConnectivityManager connManager;
    private SoftApManager apManager = null;

    private static WifiHelper instance;
    public static WifiHelper getInstance(Context ctx) {
        if (instance == null) instance = new WifiHelper(ctx);
        return instance;
    }
    private WifiHelper(Context ctx) {
        wifiManager = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
        connManager = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        try {
            apManager = SoftApManager.getInstance(ctx);
        } catch (NoSuchMethodException e) {
            Toast.makeText(ctx, "Soft AP not available.", Toast.LENGTH_LONG).show();
        }
    }

    public WifiManager getWifiManager() {
        return wifiManager;
    }
    public SoftApManager getSoftApManager() {
        return apManager;
    }
    public ConnectivityManager getConnectionManager() {
        return connManager;
    }

    /*
     * ConnectionManager related
     */
    public NetworkInfo getNetworkInfo() {
        return connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
    }
    public boolean isConnected() {
        NetworkInfo info = getNetworkInfo();
        return info != null && info.isConnected();
    }
    public boolean isConnectedOrConnecting() {
        NetworkInfo info = getNetworkInfo();
        return info != null && info.isConnectedOrConnecting();
    }
    public boolean isAvailable() {
        NetworkInfo info = getNetworkInfo();
        return info != null && info.isAvailable();
    }

    /*
     * WifiManager basic related
     */
    public boolean setWifiEnabled(boolean enabled) {
        return wifiManager.setWifiEnabled(enabled);
    }

    public boolean isWifiEnabled() {
        return wifiManager.isWifiEnabled();
    }

    @Nullable
    public String getMacAddress() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes != null) {
                    StringBuilder res1 = new StringBuilder();
                    for (byte b : macBytes) {
                        res1.append(Integer.toHexString(b & 0xFF) + ":");
                    }

                    if (res1.length() > 0) {
                        res1.deleteCharAt(res1.length() - 1);
                    }
                    return res1.toString();
                }
            }
        } catch (Exception ex) {
            //handle exception
        }
        WifiInfo info = wifiManager.getConnectionInfo();
        return info == null ? null : info.getMacAddress();
    }

    @Nullable
    public String getIpAddress() {
        WifiInfo info = wifiManager.getConnectionInfo();
        if (info == null) return null;

        int ipAddress = info.getIpAddress();
        if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
            ipAddress = Integer.reverseBytes(ipAddress);
        }
        byte[] ipByteArray = BigInteger.valueOf(ipAddress).toByteArray();

        String ipAddressString;
        try {
            ipAddressString = InetAddress.getByAddress(ipByteArray).getHostAddress();
        } catch (UnknownHostException ex) {
            Log.e(TAG, "Unable to get host address.");
            ipAddressString = null;
        }
        return ipAddressString;
    }

    /*
     * SoftAccessPointManager basic state
     */
    public boolean setSoftApEnabled(boolean enabled) {
        return apManager != null && apManager.setEnabled(enabled);
    }

    public boolean isSoftApEnabled() {
        return apManager != null && apManager.isEnabled();
    }
}
