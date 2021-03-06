package com.jifalops.localization.datatypes;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Received Signal Strength raw sample.
 */
@IgnoreExtraProperties
public class RssWifi implements RefiningParams.Sample {
    public String id1, id2;
    public int rss, freq, width;
    public float dist;

    public RssWifi() {}

    public RssWifi(String id1, String id2, int rss, int freq, int width, float dist) {
        this.id1 = id1;
        this.id2 = id2;
        this.rss = rss;
        this.freq = freq;
        this.width = width;
        this.dist = dist;
    }

    public RssWifi(String[] csv) {
        id1 = csv[0];
        id2 = csv[1];
        rss = Integer.valueOf(csv[2]);
        freq = Integer.valueOf(csv[3]);
        width = Integer.valueOf(csv[4]);
        dist = Float.valueOf(csv[5]);
    }

    @Override
    public String toString() {
        return id1 +","+ id2 +","+ rss +","+ freq +","+ width +","+ dist;
    }

    @Exclude
    @Override
    public double[] getInputs() {
        return new double[] {rss, freq, width };
    }
}
