package com.jifalops.localization.datatypes;


public class RssRanging {
    public final String id1, id2;
    public final float rss, dist, range, fspl;

    public RssRanging(String id1, String id2, float rss,
                      float dist, float range, float fspl) {
        this.id1 = id1;
        this.id2 = id2;
        this.rss = rss;
        this.dist = dist;
        this.range = range;
        this.fspl = fspl;
    }

    public RssRanging(String[] csv) {
        id1 = csv[0];
        id2 = csv[1];
        rss = Float.valueOf(csv[2]);
        dist = Float.valueOf(csv[3]);
        range = Float.valueOf(csv[4]);
        fspl = Float.valueOf(csv[5]);
    }

    @Override
    public String toString() {
        return id1 +","+ id2 +","+ rss +","+ dist +","+ range +","+ fspl;
    }
}
