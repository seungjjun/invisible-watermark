package com.seungjjun.watermark.core.dct.color;

import java.awt.image.BufferedImage;
import lombok.Getter;

@Getter
public class YCbCrBlock {

    private final int blockSize;
    private final double[][] yChannel;
    private final int[][] cbChannel;
    private final int[][] crChannel;

    private YCbCrBlock(int blockSize, double[][] yChannel, int[][] cbChannel, int[][] crChannel) {
        this.blockSize = blockSize;
        this.yChannel = yChannel;
        this.cbChannel = cbChannel;
        this.crChannel = crChannel;
    }

    public static YCbCrBlock fromImage(BufferedImage image, int x, int y, int blockSize, YCbCrConverter converter) {
        double[][] yChannel = new double[blockSize][blockSize];
        int[][] cbChannel = new int[blockSize][blockSize];
        int[][] crChannel = new int[blockSize][blockSize];

        for (int i = 0; i < blockSize; i++) {
            for (int j = 0; j < blockSize; j++) {
                int rgb = image.getRGB(x + j, y + i);
                int r = converter.extractRed(rgb);
                int g = converter.extractGreen(rgb);
                int b = converter.extractBlue(rgb);

                yChannel[i][j] = converter.calculateY(r, g, b) - converter.getYCbCrOffset();
                cbChannel[i][j] = converter.calculateCb(r, g, b);
                crChannel[i][j] = converter.calculateCr(r, g, b);
            }
        }

        return new YCbCrBlock(blockSize, yChannel, cbChannel, crChannel);
    }

    public void writeToImage(BufferedImage dest, int x, int y, YCbCrConverter converter) {
        for (int i = 0; i < blockSize; i++) {
            for (int j = 0; j < blockSize; j++) {
                double yValue = yChannel[i][j] + converter.getYCbCrOffset();
                int r = converter.calculateR(yValue, crChannel[i][j]);
                int g = converter.calculateG(yValue, cbChannel[i][j], crChannel[i][j]);
                int b = converter.calculateB(yValue, cbChannel[i][j]);

                dest.setRGB(x + j, y + i, converter.combineRGB(r, g, b));
            }
        }
    }
}
