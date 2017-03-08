package com.jifalops.localization.datatypes;

/**
 * Time of Flight raw sample.
 */
public class Tof implements NnSettings.Sample {
    public final String id1, id2;
    public final int tof;
    public final float distance;

    public Tof(String id1, String id2, int tof, float distance) {
        this.id1 = id1;
        this.id2 = id2;
        this.tof = tof;
        this.distance = distance;
    }

    public Tof(String[] csv) {
        id1 = csv[0];
        id2 = csv[1];
        tof = Integer.valueOf(csv[2]);
        distance = Float.valueOf(csv[3]);
    }

    @Override
    public String toString() {
        return id1 +","+ id2 +","+ tof +","+ distance;
    }

    @Override
    public int[] getInputs() {
        return new int[] { tof };
    }
}
