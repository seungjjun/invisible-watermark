package com.seungjjun.watermark.core.dct;

import org.jtransforms.dct.DoubleDCT_2D;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class DCTWatermark {

    private static final int BLOCK_SIZE = 8;
    // 워터마크 강도 (너무 높으면 눈에 보이고, 너무 낮으면 사라짐)
    private static final double WATERMARK_STRENGTH = 20.0;

    private final DoubleDCT_2D dctCalculator = new DoubleDCT_2D(BLOCK_SIZE, BLOCK_SIZE);

    /**
     * 텍스트 워터마크를 이미지에 삽입
     * @param originalImage 원본 이미지
     * @param watermarkText 삽입할 워터마크 텍스트
     * @param format 출력 이미지 포맷 (jpeg, png 등)
     * @return 워터마크된 이미지 바이트 배열
     */
    public byte[] embedWatermark(BufferedImage originalImage, String watermarkText, String format) throws IOException {
        // 텍스트를 비트 배열로 변환
        boolean[] watermarkBits = textToBits(watermarkText);

        // 이미지 크기 보정
        int width = (originalImage.getWidth() / BLOCK_SIZE) * BLOCK_SIZE;
        int height = (originalImage.getHeight() / BLOCK_SIZE) * BLOCK_SIZE;

        // 워터마킹 프로세스 실행
        BufferedImage watermarkedImage = processImageWithText(originalImage, width, height, watermarkBits);

        // BufferedImage -> byte[] (결과 반환)
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(watermarkedImage, format, baos);
        return baos.toByteArray();
    }

    /**
     * 이미지에서 텍스트 워터마크를 추출
     * @param watermarkedImage 워터마크가 삽입된 이미지
     * @param textLength 추출할 텍스트의 길이
     * @return 추출된 워터마크 텍스트
     */
    public String extractWatermark(BufferedImage watermarkedImage, int textLength) {
        int width = (watermarkedImage.getWidth() / BLOCK_SIZE) * BLOCK_SIZE;
        int height = (watermarkedImage.getHeight() / BLOCK_SIZE) * BLOCK_SIZE;

        // 필요한 비트 수 계산 (textLength * 8 bits per character)
        int totalBits = textLength * 8;
        boolean[] extractedBits = new boolean[totalBits];

        int bitIndex = 0;
        outerLoop:
        for (int y = 0; y < height && bitIndex < totalBits; y += BLOCK_SIZE) {
            for (int x = 0; x < width && bitIndex < totalBits; x += BLOCK_SIZE) {
                extractedBits[bitIndex] = extractBitFromBlock(watermarkedImage, x, y);
                bitIndex++;
            }
        }

        return bitsToText(extractedBits);
    }

    private boolean[] textToBits(String text) {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        boolean[] bits = new boolean[bytes.length * 8];

        for (int i = 0; i < bytes.length; i++) {
            for (int j = 0; j < 8; j++) {
                bits[i * 8 + j] = ((bytes[i] >> (7 - j)) & 1) == 1;
            }
        }

        return bits;
    }

    private String bitsToText(boolean[] bits) {
        byte[] bytes = new byte[bits.length / 8];

        for (int i = 0; i < bytes.length; i++) {
            byte b = 0;
            for (int j = 0; j < 8; j++) {
                if (bits[i * 8 + j]) {
                    b |= (1 << (7 - j));
                }
            }
            bytes[i] = b;
        }

        return new String(bytes, StandardCharsets.UTF_8);
    }

    private BufferedImage processImageWithText(BufferedImage img, int width, int height, boolean[] watermarkBits) {
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        int bitIndex = 0;
        for (int y = 0; y < height; y += BLOCK_SIZE) {
            for (int x = 0; x < width; x += BLOCK_SIZE) {
                boolean bit = (bitIndex < watermarkBits.length) ? watermarkBits[bitIndex] : false;
                embedWatermarkBitToBlock(img, result, x, y, bit);
                bitIndex++;
            }
        }

        return result;
    }

    private void embedWatermarkBitToBlock(BufferedImage src, BufferedImage dest, int x, int y, boolean bit) {
        double[][] yChannel = new double[BLOCK_SIZE][BLOCK_SIZE];
        int[][] cbChannel = new int[BLOCK_SIZE][BLOCK_SIZE];
        int[][] crChannel = new int[BLOCK_SIZE][BLOCK_SIZE];

        // RGB -> YCbCr 변환 및 채널 분리
        for (int i = 0; i < BLOCK_SIZE; i++) {
            for (int j = 0; j < BLOCK_SIZE; j++) {
                int rgb = src.getRGB(x + j, y + i);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                yChannel[i][j] = 0.299 * r + 0.587 * g + 0.114 * b;
                cbChannel[i][j] = (int) (-0.169 * r - 0.331 * g + 0.500 * b + 128);
                crChannel[i][j] = (int) (0.500 * r - 0.419 * g - 0.081 * b + 128);
                yChannel[i][j] -= 128.0;
            }
        }

        // DCT 변환
        double[] flatY = flatten(yChannel);
        dctCalculator.forward(flatY, true);

        // 워터마크 비트 삽입
        // 비트가 1이면 +STRENGTH, 0이면 -STRENGTH
        int targetIndex = 4 * BLOCK_SIZE + 4;
        flatY[targetIndex] += bit ? WATERMARK_STRENGTH : -WATERMARK_STRENGTH;

        // IDCT 변환
        dctCalculator.inverse(flatY, true);

        // YCbCr -> RGB 복원
        double[][] processedY = unflatten(flatY);
        for (int i = 0; i < BLOCK_SIZE; i++) {
            for (int j = 0; j < BLOCK_SIZE; j++) {
                double Y = processedY[i][j] + 128.0;
                double Cb = cbChannel[i][j];
                double Cr = crChannel[i][j];

                int r = clamp(Y + 1.402 * (Cr - 128));
                int g = clamp(Y - 0.34414 * (Cb - 128) - 0.71414 * (Cr - 128));
                int b = clamp(Y + 1.772 * (Cb - 128));

                int newRgb = (r << 16) | (g << 8) | b;
                dest.setRGB(x + j, y + i, newRgb);
            }
        }
    }

    private boolean extractBitFromBlock(BufferedImage src, int x, int y) {
        double[][] yChannel = new double[BLOCK_SIZE][BLOCK_SIZE];

        // RGB -> YCbCr 변환 (Y 채널만 필요)
        for (int i = 0; i < BLOCK_SIZE; i++) {
            for (int j = 0; j < BLOCK_SIZE; j++) {
                int rgb = src.getRGB(x + j, y + i);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                yChannel[i][j] = 0.299 * r + 0.587 * g + 0.114 * b - 128.0;
            }
        }

        // DCT 변환
        double[] flatY = flatten(yChannel);
        dctCalculator.forward(flatY, true);

        // (4,4) 위치의 DCT 계수 확인
        int targetIndex = 4 * BLOCK_SIZE + 4;
        double coefficient = flatY[targetIndex];

        // 계수가 양수면 비트 1, 음수면 비트 0
        return coefficient > 0;
    }

    private int clamp(double val) {
        return Math.max(0, Math.min(255, (int) val));
    }

    private double[] flatten(double[][] arr) {
        double[] res = new double[BLOCK_SIZE * BLOCK_SIZE];
        for (int i = 0; i < BLOCK_SIZE; i++)
            System.arraycopy(arr[i], 0, res, i * BLOCK_SIZE, BLOCK_SIZE);
        return res;
    }

    private double[][] unflatten(double[] arr) {
        double[][] res = new double[BLOCK_SIZE][BLOCK_SIZE];
        for (int i = 0; i < BLOCK_SIZE; i++)
            System.arraycopy(arr, i * BLOCK_SIZE, res[i], 0, BLOCK_SIZE);
        return res;
    }
}
