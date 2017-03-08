package com.jifalops.localization.datatypes;

/**
 * Received Signal Strength raw sample.
 */
public class RssWifi implements RangingParams.Sample {
    public final String id1, id2;
    public final int rssi, freq, width;
    public final float distance;

    public RssWifi(String id1, String id2, int rssi, int freq, int width, float distance) {
        this.id1 = id1;
        this.id2 = id2;
        this.rssi = rssi;
        this.freq = freq;
        this.width = width;
        this.distance = distance;
    }

    public RssWifi(String[] csv) {
        id1 = csv[0];
        id2 = csv[1];
        rssi = Integer.valueOf(csv[2]);
        freq = Integer.valueOf(csv[3]);
        width = Integer.valueOf(csv[4]);
        distance = Float.valueOf(csv[5]);
    }

    @Override
    public String toString() {
        return id1 +","+ id2 +","+ rssi +","+ freq +","+ width +","+ distance;
    }

    @Override
    public double[] getInputs() {
        return new double[] { rssi, freq, width };
    }
}
