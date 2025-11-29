package com.seungjjun.watermark.core.basic;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class RawPixelAnalyzer {

    public static void main(String[] args) {
        try {
            BufferedImage image = ImageIO.read(new File("./image/test.jpeg"));

            printImageInfo(image);

            int samplePixelRgb = image.getRGB(0, 1);
            System.out.println("\n=== Pixel (0, 1) Analysis ===");

            System.out.println("\nColor Class");
            printRgbUsingColorClass(samplePixelRgb);

            System.out.println("\nBit Shifting");
            printRgbUsingBitShift(samplePixelRgb);

        } catch (IOException e) {
            System.err.println("Failed to read image file: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private static void printImageInfo(BufferedImage image) {
        System.out.println("=== Image Information ===");
        System.out.println("Width: " + image.getWidth());
        System.out.println("Height: " + image.getHeight());
    }

    private static void printRgbUsingColorClass(int rgb) {
        Color color = new Color(rgb);
        System.out.printf("ARGB: A=%d, R=%d, G=%d, B=%d%n",
            color.getAlpha(), color.getRed(), color.getGreen(), color.getBlue());
    }

    private static void printRgbUsingBitShift(int rgb) {
        int alpha = (rgb >> 24) & 0xFF;
        int red   = (rgb >> 16) & 0xFF;
        int green = (rgb >> 8) & 0xFF;
        int blue  = rgb & 0xFF;

        System.out.printf("ARGB: A=%d, R=%d, G=%d, B=%d%n", alpha, red, green, blue);
    }
}
