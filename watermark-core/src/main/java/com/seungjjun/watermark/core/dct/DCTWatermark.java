package com.seungjjun.watermark.core.dct;

import com.seungjjun.watermark.core.dct.color.YCbCrBlock;
import com.seungjjun.watermark.core.dct.color.YCbCrConverter;
import com.seungjjun.watermark.core.dct.strategy.AdditiveDCTStrategy;
import com.seungjjun.watermark.core.dct.strategy.WatermarkStrategy;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class DCTWatermark {

    private static final int BITS_PER_BYTE = 8;
    private static final int DEFAULT_BLOCK_SIZE = 8;
    private static final double DEFAULT_STRENGTH = 20.0;
    private static final int DEFAULT_DCT_POSITION = 4;

    private final DCTProcessor dctProcessor;
    private final YCbCrConverter colorConverter;

    public DCTWatermark() {
        this(new AdditiveDCTStrategy());
    }

    public DCTWatermark(WatermarkStrategy strategy) {
        this.dctProcessor = new DCTProcessor(DEFAULT_BLOCK_SIZE, strategy);
        this.colorConverter = new YCbCrConverter();
    }

    public byte[] embedWatermark(BufferedImage originalImage, String watermarkText, String format) throws IOException {
        return embedWatermark(originalImage, watermarkText, format, DEFAULT_STRENGTH);
    }

    public byte[] embedWatermark(BufferedImage originalImage, String watermarkText, String format, double strength) throws IOException {
        boolean[] watermarkBits = textToBits(watermarkText);

        int width = (originalImage.getWidth() / DEFAULT_BLOCK_SIZE) * DEFAULT_BLOCK_SIZE;
        int height = (originalImage.getHeight() / DEFAULT_BLOCK_SIZE) * DEFAULT_BLOCK_SIZE;

        BufferedImage watermarkedImage = processImageWithText(originalImage, width, height, watermarkBits, strength);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(watermarkedImage, format, baos);
        return baos.toByteArray();
    }

    public String extractWatermark(BufferedImage watermarkedImage, int textLength) {
        int width = (watermarkedImage.getWidth() / DEFAULT_BLOCK_SIZE) * DEFAULT_BLOCK_SIZE;
        int height = (watermarkedImage.getHeight() / DEFAULT_BLOCK_SIZE) * DEFAULT_BLOCK_SIZE;

        int totalBits = textLength * BITS_PER_BYTE;
        boolean[] extractedBits = new boolean[totalBits];

        int bitIndex = 0;
        for (int y = 0; y < height && bitIndex < totalBits; y += DEFAULT_BLOCK_SIZE) {
            for (int x = 0; x < width && bitIndex < totalBits; x += DEFAULT_BLOCK_SIZE) {
                extractedBits[bitIndex] = extractBitFromBlock(watermarkedImage, x, y);
                bitIndex++;
            }
        }

        return bitsToText(extractedBits);
    }

    private boolean[] textToBits(String text) {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        boolean[] bits = new boolean[bytes.length * BITS_PER_BYTE];

        for (int i = 0; i < bytes.length; i++) {
            for (int j = 0; j < BITS_PER_BYTE; j++) {
                bits[i * BITS_PER_BYTE + j] = ((bytes[i] >> (BITS_PER_BYTE - 1 - j)) & 1) == 1;
            }
        }

        return bits;
    }

    private String bitsToText(boolean[] bits) {
        byte[] bytes = new byte[bits.length / BITS_PER_BYTE];

        for (int i = 0; i < bytes.length; i++) {
            byte b = 0;
            for (int j = 0; j < BITS_PER_BYTE; j++) {
                if (bits[i * BITS_PER_BYTE + j]) {
                    b |= (1 << (BITS_PER_BYTE - 1 - j));
                }
            }
            bytes[i] = b;
        }

        return new String(bytes, StandardCharsets.UTF_8);
    }

    private BufferedImage processImageWithText(BufferedImage img, int width, int height, boolean[] watermarkBits, double strength) {
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        int bitIndex = 0;
        for (int y = 0; y < height; y += DEFAULT_BLOCK_SIZE) {
            for (int x = 0; x < width; x += DEFAULT_BLOCK_SIZE) {
                boolean bit = (bitIndex < watermarkBits.length) ? watermarkBits[bitIndex] : false;
                embedWatermarkBitToBlock(img, result, x, y, bit, strength);
                bitIndex++;
            }
        }

        return result;
    }

    private void embedWatermarkBitToBlock(BufferedImage src, BufferedImage dest, int x, int y, boolean bit, double strength) {
        YCbCrBlock block = YCbCrBlock.fromImage(src, x, y, DEFAULT_BLOCK_SIZE, colorConverter);
        dctProcessor.embedWatermarkBit(block.getYChannel(), bit, strength, DEFAULT_DCT_POSITION);
        block.writeToImage(dest, x, y, colorConverter);
    }

    private boolean extractBitFromBlock(BufferedImage src, int x, int y) {
        YCbCrBlock block = YCbCrBlock.fromImage(src, x, y, DEFAULT_BLOCK_SIZE, colorConverter);
        return dctProcessor.extractWatermarkBit(block.getYChannel(), DEFAULT_DCT_POSITION);
    }

}
