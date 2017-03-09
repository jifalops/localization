package com.jifalops.localization.datatypes;

import android.text.TextUtils;
import android.util.Log;

import java.util.Arrays;

/**
 * inputs, hidden, maxRange, weights...
 */
public class RangingParams {
    public final int inputs, hidden, maxRange;
    public final double[] weights;

    public RangingParams(int inputs, int hidden, int maxRange, double[] weights) {
        this.inputs = inputs;
        this.hidden = hidden;
        this.maxRange = maxRange;
        this.weights = weights;
    }

    public RangingParams(String[] csv) {
        inputs = Integer.valueOf(csv[0]);
        hidden = Integer.valueOf(csv[1]);
        maxRange = Integer.valueOf(csv[2]);

        String[] tmp = Arrays.copyOfRange(csv, 3, csv.length);
        weights = new double[tmp.length];
        for (int i = 0; i < tmp.length; ++i) {
            weights[i] = Double.valueOf(tmp[i]);
        }
    }

    @Override
    public String toString() {
        String[] tmp = new String[weights.length];
        for (int i = 0; i < tmp.length; ++i) {
            tmp[i] = String.valueOf(weights[i]);
        }
        return inputs + "," + hidden + "," + maxRange +","+ TextUtils.join(",", tmp);
    }


    public float estimateRange(double[] inputs) {
        float range = (float) unscaleOutput(calcOutput(scaleInputs(inputs)));
        if (range == 0) Log.d("RangingParams", "Range is 0m");
        // In RssSamplingHelper, a range of 0 is used to mean no range estimate available.
        return range == 0 ? 0.01f : range;
    }

    public static float freeSpacePathLoss(double levelInDb, double freqInMHz) {
        double exp = (27.55 - (20 * Math.log10(freqInMHz)) + Math.abs(levelInDb)) / 20.0;
        return (float) Math.pow(10.0, exp);
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
}
