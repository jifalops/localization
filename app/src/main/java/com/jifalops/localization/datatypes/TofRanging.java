package com.jifalops.localization.datatypes;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class TofRanging {
    public String id1, id2;
    public float tof, dist, range, fspl;

    public TofRanging() {}

    public TofRanging(String id1, String id2, float tof,
                      float dist, float range, float fspl) {
        this.id1 = id1;
        this.id2 = id2;
        this.tof = tof;
        this.dist = dist;
        this.range = range;
        this.fspl = fspl;
    }

    public TofRanging(String[] csv) {
        id1 = csv[0];
        id2 = csv[1];
        tof = Float.valueOf(csv[2]);
        dist = Float.valueOf(csv[3]);
        range = Float.valueOf(csv[4]);
        fspl = Float.valueOf(csv[5]);
    }

    @Override
    public String toString() {
        return id1 +","+ id2 +","+ tof +","+ dist +","+ range +","+ fspl;
    }
}
