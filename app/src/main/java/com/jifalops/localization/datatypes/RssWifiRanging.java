package com.jifalops.localization.datatypes;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class RssWifiRanging {
    public String id1, id2;
    public float rss, freq, width, dist, range, fspl;

    public RssWifiRanging() {}

    public RssWifiRanging(String id1, String id2, float rss, float freq, float width,
                          float dist, float range, float fspl) {
        this.id1 = id1;
        this.id2 = id2;
        this.rss = rss;
        this.freq = freq;
        this.width = width;
        this.dist = dist;
        this.range = range;
        this.fspl = fspl;
    }

    public RssWifiRanging(String[] csv) {
        id1 = csv[0];
        id2 = csv[1];
        rss = Float.valueOf(csv[2]);
        freq = Float.valueOf(csv[3]);
        width = Float.valueOf(csv[4]);
        dist = Float.valueOf(csv[5]);
        range = Float.valueOf(csv[6]);
        fspl = Float.valueOf(csv[7]);
    }

    @Override
    public String toString() {
        return id1 +","+ id2 +","+ rss +","+ freq +","+ width +","+ dist +","+ range +","+ fspl;
    }
}
