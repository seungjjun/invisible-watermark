package com.seungjjun.watermark.core.dct;

import com.seungjjun.watermark.core.dct.strategy.WatermarkStrategy;
import org.jtransforms.dct.DoubleDCT_2D;

public class DCTProcessor {

    private final int blockSize;
    private final DoubleDCT_2D dctCalculator;
    private final WatermarkStrategy strategy;

    public DCTProcessor(int blockSize, WatermarkStrategy strategy) {
        this.blockSize = blockSize;
        this.dctCalculator = new DoubleDCT_2D(blockSize, blockSize);
        this.strategy = strategy;
    }

    public void embedWatermarkBit(double[][] yChannel, boolean bit, double strength, int targetPosition) {
        double[] flatY = flatten(yChannel);
        dctCalculator.forward(flatY, true);

        strategy.embedBit(flatY, bit, strength, targetPosition, blockSize);

        dctCalculator.inverse(flatY, true);
        unflattenInto(flatY, yChannel);
    }

    public boolean extractWatermarkBit(double[][] yChannel, int targetPosition) {
        double[] flatY = flatten(yChannel);
        dctCalculator.forward(flatY, true);

        return strategy.extractBit(flatY, targetPosition, blockSize);
    }

    private double[] flatten(double[][] arr) {
        double[] res = new double[blockSize * blockSize];
        for (int i = 0; i < blockSize; i++) {
            System.arraycopy(arr[i], 0, res, i * blockSize, blockSize);
        }
        return res;
    }

    private void unflattenInto(double[] arr, double[][] dest) {
        for (int i = 0; i < blockSize; i++) {
            System.arraycopy(arr, i * blockSize, dest[i], 0, blockSize);
        }
    }
}
