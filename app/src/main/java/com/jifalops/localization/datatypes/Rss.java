package com.jifalops.localization.datatypes;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Received Signal Strength raw sample.
 */
@IgnoreExtraProperties
public class Rss implements RefiningParams.Sample {
    public  String id1, id2;
    public int rss;
    public float dist;

    public Rss() {}

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

    @Exclude
    @Override
    public double[] getInputs() {
        return new double[] {rss};
    }
}
