package com.seungjjun.watermark.core.dct.strategy;

public class AdditiveDCTStrategy implements WatermarkStrategy {

    @Override
    public void embedBit(double[] dctCoefficients, boolean bit, double strength, int targetPosition, int blockSize) {
        int targetIndex = targetPosition * blockSize + targetPosition;
        dctCoefficients[targetIndex] += bit ? strength : -strength;
    }

    @Override
    public boolean extractBit(double[] dctCoefficients, int targetPosition, int blockSize) {
        int targetIndex = targetPosition * blockSize + targetPosition;
        return dctCoefficients[targetIndex] > 0;
    }
}
