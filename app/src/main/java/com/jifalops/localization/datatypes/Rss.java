package com.jifalops.localization.datatypes;

/**
 * Received Signal Strength raw sample.
 */
public class Rss implements RefiningParams.Sample {
    public final String id1, id2;
    public final int rss;
    public final float dist;

    public Rss(String id1, String id2, int rss, float dist) {
        this.id1 = id1;
        this.id2 = id2;
        this.rss = rss;
        this.dist = dist;
    }

    public Rss(String[] csv) {
        id1 = csv[0];
        id2 = csv[1];
        rss = Integer.valueOf(csv[2]);
        dist = Float.valueOf(csv[3]);
    }

    @Override
    public String toString() {
        return id1 +","+ id2 +","+ rss +","+ dist;
    }

    @Override
    public double[] getInputs() {
        return new double[] {rss};
    }
}
