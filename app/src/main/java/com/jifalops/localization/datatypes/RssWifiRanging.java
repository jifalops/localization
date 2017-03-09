package com.jifalops.localization.datatypes;


public class RssWifiRanging {
    public final String id1, id2;
    public final float rss, freq, width, dist, range, fspl;

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
        rss = Integer.valueOf(csv[2]);
        freq = Integer.valueOf(csv[3]);
        width = Integer.valueOf(csv[4]);
        dist = Float.valueOf(csv[5]);
        range = Float.valueOf(csv[6]);
        fspl = Float.valueOf(csv[7]);
    }

    @Override
    public String toString() {
        return id1 +","+ id2 +","+ rss +","+ freq +","+ width +","+ dist +","+ range +","+ fspl;
    }
}