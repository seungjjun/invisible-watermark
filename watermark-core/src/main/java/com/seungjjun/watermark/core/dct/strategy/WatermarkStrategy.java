package com.seungjjun.watermark.core.dct.strategy;

public interface WatermarkStrategy {

    void embedBit(double[] dctCoefficients, boolean bit, double strength, int targetPosition, int blockSize);

    boolean extractBit(double[] dctCoefficients, int targetPosition, int blockSize);
}
