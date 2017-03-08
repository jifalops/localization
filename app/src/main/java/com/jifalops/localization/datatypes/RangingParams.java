package com.jifalops.localization.datatypes;

import android.text.TextUtils;
import android.util.Log;

import com.jifalops.localization.util.Stats;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * samples, method, drop, inputs, hidden, maxRange, weights...
 */
public class RangingParams {
    public static final String METHOD_MEDIAN = "median";
    public static final String METHOD_MEAN   = "mean";
    public final String method;
    public final int samples, drop, inputs, hidden, maxRange;
    public final double[] weights;

    public final Sampler sampler;

    public RangingParams(int samples, String method, int drop, int inputs, int hidden, int maxRange, double[] weights) {
        this.samples = samples;
        this.method = method;
        this.drop = drop;
        this.inputs = inputs;
        this.hidden = hidden;
        this.maxRange = maxRange;
        this.weights = weights;

        sampler = new Sampler();
    }

    public RangingParams(String[] csv) {
        samples = Integer.valueOf(csv[0]);
        method = csv[1];
        drop = Integer.valueOf(csv[2]);
        inputs = Integer.valueOf(csv[3]);
        hidden = Integer.valueOf(csv[4]);
        maxRange = Integer.valueOf(csv[5]);

        String[] tmp = Arrays.copyOfRange(csv, 6, csv.length);
        weights = new double[tmp.length];
        for (int i = 0; i < tmp.length; ++i) {
            weights[i] = Double.valueOf(tmp[i]);
        }

        sampler = new Sampler();
    }

    @Override
    public String toString() {
        String[] tmp = new String[weights.length];
        for (int i = 0; i < tmp.length; ++i) {
            tmp[i] = String.valueOf(weights[i]);
        }
        return samples + "," + method + "," + drop + "," + inputs + "," + hidden + "," + maxRange +","+ TextUtils.join(",", tmp);
    }


    public float estimateRange(double[] inputs) {
        float range = (float) unscaleOutput(calcOutput(scaleInputs(inputs)));
        if (range == 0) Log.d("RangingParams", "Range is 0m");
        // In RssSamplingHelper, a range of 0 is used to mean no range estimate available.
        return range == 0 ? 0.01f : range;
    }

    private double[] scaleInputs(double[] unscaled) {
        double[] scaled = new double[inputs];
        for (int i = 0; i < inputs; ++i) {
            scaled[i] = scaleInput(unscaled[i], 0, maxRange);
        }
        return scaled;
    }

    /** Scale an input to between -1 and 1. */
    private double scaleInput(double value, double min, double max) {
        // scaledMin + (value - unscaledMin) * (scaledMax - scaledMin) / (unscaledMax - unscaledMin)
        return -1 + (value - min) * 2 / (max - min);
    }

    /** Unscale an output from between 0 and 1. */
    private double unscaleOutput(double value) {
        // unscaledMin + (value - scaledMin) * (unscaledMax - unscaledMin) / (scaledMax - scaledMin)
        return value * maxRange;
    }

    private double calcOutput(double[] scaledInputs) {
        int numOutputs = 1;
        int hiddenBiasesStart = inputs * hidden;
        int hiddenToOutputStart = hiddenBiasesStart + hidden;
        int outputBiasesStart = hiddenToOutputStart + hidden * numOutputs;
        double[] outputs = new double[numOutputs];
        double[] gamma = new double[hidden];
        double[] z = new double[hidden];

        int start;
        // Weights for connections between input and hidden neurons.
        for (int i = 0; i < inputs; i++) {
            start = i * hidden;
            for (int j = 0; j < hidden; j++) {
                gamma[j] += weights[start + j] * scaledInputs[i];
            }
        }

        for (int j = 0; j < hidden; j++) {
            // Weights for the biases of hidden neurons.
            gamma[j] += weights[hiddenBiasesStart + j];

            // Sigmoid activation
            z[j] = 1 / (1 + Math.exp(-gamma[j]));

            // Weights for connections between hidden and output neurons.
            start = hiddenToOutputStart + j * numOutputs;
            for (int k = 0; k < numOutputs; k++) {
                outputs[k] += weights[start + k] * z[j];
            }
        }

        // Weights for the biases of output neurons.
        for (int k = 0; k < numOutputs; k++) {
            outputs[k] += weights[outputBiasesStart + k];
        }

        return outputs[0];
    }


    public interface Sample {
        double[] getInputs();
    }


    public class Sampler {
        private final ArrayList<Sample> rawSampleList = new ArrayList<>(samples);
        private Sampler() {}

        /**
         * @param sample A raw sample.
         * @return A ranging estimate if this sample fulfills the NN settings requirement, or -x
         * where x is how many more raw samples are needed to make a ranging sample.
         */
        public synchronized float add(Sample sample) {
            if (rawSampleList.add(sample) && rawSampleList.size() == samples) {
                double[] inputSamples; // Mean or median of rssi, freq, width, or tof.
                double[] rangingInputs = new double[inputs];
                switch (method) {
                    case METHOD_MEDIAN:
                        for (int j = 0; j < inputs; ++j) {
                            inputSamples = new double[samples];
                            for (int i = 0; i < samples; ++i) {
                                inputSamples[i] = rawSampleList.get(i).getInputs()[j];
                            }
                            rangingInputs[j] = Stats.median(inputSamples, drop);
                        }
                        break;
                    case METHOD_MEAN:
                        for (int j = 0; j < inputs; ++j) {
                            inputSamples = new double[samples];
                            for (int i = 0; i < samples; ++i) {
                                inputSamples[i] = rawSampleList.get(i).getInputs()[j];
                            }
                            rangingInputs[j] = Stats.mean(inputSamples, drop);
                        }
                        break;
                }
                rawSampleList.clear();
                return estimateRange(rangingInputs);
            }
            return -1 * (samples - rawSampleList.size());
        }
    }
}
