package com.jifalops.localization.datatypes;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class RssBtleRanging {
    public String id1, id2;
    public  float rss, txPower, dist, range, fspl;

    public RssBtleRanging() {}

    public RssBtleRanging(String id1, String id2, float rss, float txPower,
                          float dist, float range, float fspl) {
        this.id1 = id1;
        this.id2 = id2;
        this.rss = rss;
        this.txPower = txPower;
        this.dist = dist;
        this.range = range;
        this.fspl = fspl;
    }

    public RssBtleRanging(String[] csv) {
        id1 = csv[0];
        id2 = csv[1];
        rss = Float.valueOf(csv[2]);
        txPower = Float.valueOf(csv[3]);
        dist = Float.valueOf(csv[4]);
        range = Float.valueOf(csv[5]);
        fspl = Float.valueOf(csv[6]);
    }

    @Override
    public String toString() {
        return id1 +","+ id2 +","+ rss +","+ txPower +","+ dist +","+ range +","+ fspl;
    }
}
