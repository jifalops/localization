package com.jifalops.localization.datatypes;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Time of Flight raw sample.
 */
@IgnoreExtraProperties
public class Tof implements RefiningParams.Sample {
    public String id1, id2;
    public int tof;
    public float dist;

    public Tof() {}

    public Tof(String id1, String id2, int tof, float dist) {
        this.id1 = id1;
        this.id2 = id2;
        this.tof = tof;
        this.dist = dist;
    }

    public Tof(String[] csv) {
        id1 = csv[0];
        id2 = csv[1];
        tof = Integer.valueOf(csv[2]);
        dist = Float.valueOf(csv[3]);
    }

    @Override
    public String toString() {
        return id1 +","+ id2 +","+ tof +","+ dist;
    }

    @Exclude
    @Override
    public double[] getInputs() {
        return new double[] { tof };
    }
}
