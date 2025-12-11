package com.seungjjun.watermark.core.lsb.embedder;

import com.seungjjun.watermark.core.lsb.model.WatermarkData;
import com.seungjjun.watermark.core.lsb.util.BitManipulator;

import java.awt.image.BufferedImage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LSBEmbedder {

    private static final int BYTE_MASK = 0xFF;
    private static final int ALPHA_SHIFT = 24;
    private static final int RED_SHIFT = 16;
    private static final int GREEN_SHIFT = 8;
    private static final int BLUE_SHIFT = 0;

    private final int bitDepth;

    public LSBEmbedder(int bitDepth) {
        // 4비트 이상은 원본 손상 심함
        if (bitDepth < 1 || bitDepth > 8) {
            throw new IllegalArgumentException("Bit depth must be between 1 and 8");
        }
        this.bitDepth = bitDepth;
    }

    public LSBEmbedder() {
        this(1);
    }

    public void embed(BufferedImage image, WatermarkData data) {
        byte[] allBytes = data.toBytes();
        int width = image.getWidth();
        int height = image.getHeight();

        int totalBits = allBytes.length * 8;
        // 3채널 사용 (RGB)
        int availableBits = width * height * 3 * bitDepth;

        if (totalBits > availableBits) {
            log.error("Image is too small to embed data. Required: {} bits, Available: {} bits", totalBits, availableBits);
            throw new IllegalArgumentException("Image is too small to embed data");
        }

        int bitIndex = 0;
        int mask = BitManipulator.createMask(bitDepth);

        for (int y = 0; y < height && bitIndex < totalBits; y++) {
            for (int x = 0; x < width && bitIndex < totalBits; x++) {
                int rgb = image.getRGB(x, y);

                int alpha = (rgb >> ALPHA_SHIFT) & BYTE_MASK;
                int red = (rgb >> RED_SHIFT) & BYTE_MASK;
                int green = (rgb >> GREEN_SHIFT) & BYTE_MASK;
                int blue = (rgb >> BLUE_SHIFT) & BYTE_MASK;

                red = embedBitsIntoChannel(red, allBytes, bitIndex, mask, totalBits);
                bitIndex += Math.min(bitDepth, totalBits - bitIndex);

                if (bitIndex < totalBits) {
                    green = embedBitsIntoChannel(green, allBytes, bitIndex, mask, totalBits);
                    bitIndex += Math.min(bitDepth, totalBits - bitIndex);
                }

                if (bitIndex < totalBits) {
                    blue = embedBitsIntoChannel(blue, allBytes, bitIndex, mask, totalBits);
                    bitIndex += Math.min(bitDepth, totalBits - bitIndex);
                }

                int newRgb = (alpha << ALPHA_SHIFT) | (red << RED_SHIFT) |
                    (green << GREEN_SHIFT) | (blue << BLUE_SHIFT);
                image.setRGB(x, y, newRgb);
            }
        }
    }

    private int embedBitsIntoChannel(int channel, byte[] bytes, int bitIndex, int mask, int totalBits) {
        int bits = 0;

        for (int i = 0; i < bitDepth; i++) {
            int currentBitIndex = bitIndex + i;
            int bit = 0;
            if (currentBitIndex < totalBits) {
                bit = BitManipulator.getBit(bytes, currentBitIndex);
            }
            bits = (bits << 1) | bit;
        }

        return (channel & mask) | bits;
    }
}
