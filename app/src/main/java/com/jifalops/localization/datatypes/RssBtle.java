package com.jifalops.localization.datatypes;

/**
 * Received Signal Strength raw sample.
 */
public class RssBtle implements RefiningParams.Sample {
    public final String id1, id2;
    public final int rss, txPower;
    public final float dist;

    public RssBtle(String id1, String id2, int rss, int txPower, float dist) {
        this.id1 = id1;
        this.id2 = id2;
        this.rss = rss;
        this.txPower = txPower;
        this.dist = dist;
    }

    public RssBtle(String[] csv) {
        id1 = csv[0];
        id2 = csv[1];
        rss = Integer.valueOf(csv[2]);
        txPower = Integer.valueOf(csv[3]);
        dist = Float.valueOf(csv[4]);
    }

    @Override
    public String toString() {
        return id1 +","+ id2 +","+ rss +","+ txPower +","+ dist;
    }

    @Override
    public double[] getInputs() {
        return new double[] {rss, txPower };
    }
}
