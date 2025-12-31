package com.seungjjun.watermark.core.dct.color;

public class YCbCrConverter {

    private static final int RGB_RED_SHIFT = 16;
    private static final int RGB_GREEN_SHIFT = 8;
    private static final int RGB_MASK = 0xFF;
    private static final int RGB_MIN = 0;
    private static final int RGB_MAX = 255;

    private static final double YCBCR_OFFSET = 128.0;
    private static final double Y_FROM_R = 0.299;
    private static final double Y_FROM_G = 0.587;
    private static final double Y_FROM_B = 0.114;
    private static final double CB_FROM_R = -0.169;
    private static final double CB_FROM_G = -0.331;
    private static final double CB_FROM_B = 0.500;
    private static final double CR_FROM_R = 0.500;
    private static final double CR_FROM_G = -0.419;
    private static final double CR_FROM_B = -0.081;

    private static final double R_FROM_Y = 1.0;
    private static final double R_FROM_CR = 1.402;
    private static final double G_FROM_Y = 1.0;
    private static final double G_FROM_CB = -0.34414;
    private static final double G_FROM_CR = -0.71414;
    private static final double B_FROM_Y = 1.0;
    private static final double B_FROM_CB = 1.772;

    public int extractRed(int rgb) {
        return (rgb >> RGB_RED_SHIFT) & RGB_MASK;
    }

    public int extractGreen(int rgb) {
        return (rgb >> RGB_GREEN_SHIFT) & RGB_MASK;
    }

    public int extractBlue(int rgb) {
        return rgb & RGB_MASK;
    }

    public double calculateY(int r, int g, int b) {
        return Y_FROM_R * r + Y_FROM_G * g + Y_FROM_B * b;
    }

    public int calculateCb(int r, int g, int b) {
        return (int) (CB_FROM_R * r + CB_FROM_G * g + CB_FROM_B * b + YCBCR_OFFSET);
    }

    public int calculateCr(int r, int g, int b) {
        return (int) (CR_FROM_R * r + CR_FROM_G * g + CR_FROM_B * b + YCBCR_OFFSET);
    }

    public int calculateR(double y, double cr) {
        return clamp(R_FROM_Y * y + R_FROM_CR * (cr - YCBCR_OFFSET));
    }

    public int calculateG(double y, double cb, double cr) {
        return clamp(G_FROM_Y * y + G_FROM_CB * (cb - YCBCR_OFFSET) + G_FROM_CR * (cr - YCBCR_OFFSET));
    }

    public int calculateB(double y, double cb) {
        return clamp(B_FROM_Y * y + B_FROM_CB * (cb - YCBCR_OFFSET));
    }

    public int combineRGB(int r, int g, int b) {
        return (r << RGB_RED_SHIFT) | (g << RGB_GREEN_SHIFT) | b;
    }

    public double getYCbCrOffset() {
        return YCBCR_OFFSET;
    }

    private int clamp(double val) {
        return Math.max(RGB_MIN, Math.min(RGB_MAX, (int) val));
    }
}
