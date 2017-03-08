package com.jifalops.localization.datatypes;

import com.jifalops.localization.util.Stats;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * samples, dropHigh, dropLow, method
 */
public class RefiningParams {
    public static final String METHOD_MEDIAN = "median";
    public static final String METHOD_MEAN   = "mean";
    public static final String METHOD_MIN   = "min";
    public static final String METHOD_MAX   = "max";

    public final String method;
    public final int samples, dropHigh, dropLow;

    public final Sampler sampler;

    public RefiningParams(int samples, int dropHigh, int dropLow, String method) {
        this.samples = samples;
        this.dropHigh = dropHigh;
        this.dropLow = dropLow;
        this.method = method;

        sampler = new Sampler();
    }

    public RefiningParams(String[] csv) {
        samples = Integer.valueOf(csv[0]);
        dropHigh = Integer.valueOf(csv[1]);
        dropLow = Integer.valueOf(csv[2]);
        method = csv[3];

        sampler = new Sampler();
    }

    @Override
    public String toString() {
        return samples + "," + dropHigh + "," + dropLow +","+ method;
    }

    public interface Sample {
        double[] getInputs();
    }

    public class Sampler {
        private final List<Sample> list = new ArrayList<>(samples);
        private Sampler() {}
        /**
         * @return The refined sample if enough raw samples have been given
         * or null otherwise.
         */
        public synchronized Sample add(Sample sample) {
            if (list.add(sample) && list.size() == samples) {
                int fields = sample.getInputs().length;
                final double[] refined = new double[fields];
                switch (method) {
                    case METHOD_MEDIAN:
                        for (int i = 0; i < fields; ++i) {
                            refined[i] = Stats.median(samplesOfField(i));
                        }
                        break;
                    case METHOD_MEAN:
                        for (int i = 0; i < fields; ++i) {
                            refined[i] = Stats.mean(samplesOfField(i));
                        }
                        break;
                    case METHOD_MIN:
                        for (int i = 0; i < fields; ++i) {
                            refined[i] = Stats.min(samplesOfField(i));
                        }
                        break;
                    case METHOD_MAX:
                        for (int i = 0; i < fields; ++i) {
                            refined[i] = Stats.max(samplesOfField(i));
                        }
                        break;
                }
                list.clear();
                return new Sample() {
                    @Override
                    public double[] getInputs() {
                        return refined;
                    }
                };
            }
            return null;
        }

        private double[] samplesOfField(int field) {
            double[] fieldSamples = new double[samples];
            for (int i = 0; i < samples; ++i) {
                fieldSamples[i] = list.get(i).getInputs()[field];
            }
            Arrays.sort(fieldSamples);
            if (dropHigh > 0) {
                fieldSamples = Arrays.copyOfRange(fieldSamples, 0, fieldSamples.length - dropHigh);
            }
            if (dropLow > 0) {
                fieldSamples = Arrays.copyOfRange(fieldSamples, dropLow, fieldSamples.length);
            }
            return fieldSamples;
        }
    }
}
