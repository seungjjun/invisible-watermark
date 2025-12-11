package com.seungjjun.watermark.core.lsb.extractor;

import com.seungjjun.watermark.core.lsb.model.WatermarkData;
import com.seungjjun.watermark.core.lsb.util.BitManipulator;

import java.awt.image.BufferedImage;

public class LSBExtractor {

    private static final int BYTE_MASK = 0xFF;
    private static final int RED_SHIFT = 16;
    private static final int GREEN_SHIFT = 8;
    private static final int BLUE_SHIFT = 0;

    private final int bitDepth;

    public LSBExtractor(int bitDepth) {
        if (bitDepth < 1 || bitDepth > 8) {
            throw new IllegalArgumentException("Bit depth must be between 1 and 8");
        }
        this.bitDepth = bitDepth;
    }

    public LSBExtractor() {
        this(1);
    }

    public WatermarkData extract(BufferedImage image) {
        // 먼저 길이 추출 (32비트)
        byte[] lengthBytes = extractBytes(image, 4, 0);
        int dataLength = BitManipulator.bytesToInt(lengthBytes);

        // 데이터 추출
        int startBitIndex = 32;
        byte[] dataBytes = extractBytes(image, dataLength, startBitIndex);

        return WatermarkData.fromBytes(dataBytes);
    }

    private byte[] extractBytes(BufferedImage image, int byteCount, int startBitIndex) {
        byte[] bytes = new byte[byteCount];
        int bitIndex = startBitIndex;
        int totalBits = startBitIndex + (byteCount * 8);

        int width = image.getWidth();
        int height = image.getHeight();

        // 시작 픽셀 및 채널 계산
        int bitsPerPixel = 3 * bitDepth;
        int pixelIndex = startBitIndex / bitsPerPixel;
        int channelOffset = (startBitIndex % bitsPerPixel) / bitDepth;
        int bitOffset = (startBitIndex % bitsPerPixel) % bitDepth;

        int x = pixelIndex % width;
        int y = pixelIndex / width;

        for (; y < height && bitIndex < totalBits; y++) {
            for (; x < width && bitIndex < totalBits; x++) {
                int rgb = image.getRGB(x, y);

                int red = (rgb >> RED_SHIFT) & BYTE_MASK;
                int green = (rgb >> GREEN_SHIFT) & BYTE_MASK;
                int blue = (rgb >> BLUE_SHIFT) & BYTE_MASK;

                if (channelOffset == 0) {
                    bitIndex = extractBitsFromChannel(red, bytes, bitIndex, startBitIndex,
                        totalBits, bitOffset);
                    bitOffset = 0;
                    channelOffset++;
                }

                if (channelOffset == 1 && bitIndex < totalBits) {
                    bitIndex = extractBitsFromChannel(green, bytes, bitIndex, startBitIndex,
                        totalBits, bitOffset);
                    bitOffset = 0;
                    channelOffset++;
                }

                if (channelOffset == 2 && bitIndex < totalBits) {
                    bitIndex = extractBitsFromChannel(blue, bytes, bitIndex, startBitIndex,
                        totalBits, bitOffset);
                    bitOffset = 0;
                    channelOffset = 0;
                }
            }
            x = 0;
        }

        return bytes;
    }

    private int extractBitsFromChannel(int channel, byte[] bytes, int bitIndex,
                                       int startBitIndex, int totalBits, int bitOffset) {
        int extractedBits = BitManipulator.extractBits(channel, bitDepth);

        for (int i = bitDepth - 1 - bitOffset; i >= 0 && bitIndex < totalBits; i--) {
            int bit = (extractedBits >> i) & 1;
            BitManipulator.setBit(bytes, bitIndex - startBitIndex, bit);
            bitIndex++;
        }

        return bitIndex;
    }
}
