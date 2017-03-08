package com.jifalops.localization.datatypes;

/**
 * Received Signal Strength raw sample.
 */
public class RssBtle implements RangingParams.Sample {
    public final String id1, id2;
    public final int rssi, txPower;
    public final float distance;

    public RssBtle(String id1, String id2, int rssi, int txPower, float distance) {
        this.id1 = id1;
        this.id2 = id2;
        this.rssi = rssi;
        this.txPower = txPower;
        this.distance = distance;
    }

    public RssBtle(String[] csv) {
        id1 = csv[0];
        id2 = csv[1];
        rssi = Integer.valueOf(csv[2]);
        txPower = Integer.valueOf(csv[3]);
        distance = Float.valueOf(csv[4]);
    }

    @Override
    public String toString() {
        return id1 +","+ id2 +","+ rssi +","+ txPower +","+ distance;
    }

    @Override
    public double[] getInputs() {
        return new double[] { rssi, txPower };
    }
}
