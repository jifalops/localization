package com.jifalops.localization.datatypes;

/**
 * Received Signal Strength raw sample.
 */
public class Rssi implements RangingParams.Sample {
    public final String id1, id2;
    public final int rssi;
    public final float distance;

    public Rssi(String id1, String id2, int rssi, float distance) {
        this.id1 = id1;
        this.id2 = id2;
        this.rssi = rssi;
        this.distance = distance;
    }

    public Rssi(String[] csv) {
        id1 = csv[0];
        id2 = csv[1];
        rssi = Integer.valueOf(csv[2]);
        distance = Float.valueOf(csv[3]);
    }

    @Override
    public String toString() {
        return id1 +","+ id2 +","+ rssi +","+ distance;
    }

    @Override
    public int[] getInputs() {
        return new int[] { rssi };
    }
}
