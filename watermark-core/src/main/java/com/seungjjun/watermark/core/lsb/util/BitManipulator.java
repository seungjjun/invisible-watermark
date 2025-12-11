package com.seungjjun.watermark.core.lsb.util;

public class BitManipulator {

    private static final int BYTE_MASK = 0xFF;
    private static final int BITS_PER_BYTE = 8;

    // byte 배열에서 특정 비트를 추출
    public static int getBit(byte[] bytes, int bitIndex) {
        int byteIndex = bitIndex / BITS_PER_BYTE;
        int bitPosition = 7 - (bitIndex % BITS_PER_BYTE);
        return (bytes[byteIndex] >> bitPosition) & 1;
    }

    // byte 배열의 특정 비트에 값을 설정
    public static void setBit(byte[] bytes, int bitIndex, int bitValue) {
        int byteIndex = bitIndex / BITS_PER_BYTE;
        int bitPosition = 7 - (bitIndex % BITS_PER_BYTE);

        if (bitValue == 1) {
            bytes[byteIndex] |= (1 << bitPosition);
        }
    }

    public static byte[] intToBytes(int value) {
        return new byte[]{
            (byte) (value >> 24),
            (byte) (value >> 16),
            (byte) (value >> 8),
            (byte) value
        };
    }

    public static int bytesToInt(byte[] bytes) {
        return ((bytes[0] & BYTE_MASK) << 24) |
            ((bytes[1] & BYTE_MASK) << 16) |
            ((bytes[2] & BYTE_MASK) << 8) |
            (bytes[3] & BYTE_MASK);
    }

    public static int createMask(int bitDepth) {
        return (BYTE_MASK << bitDepth) & BYTE_MASK;
    }

    public static int extractBits(int value, int bitDepth) {
        return value & ((1 << bitDepth) - 1);
    }
}
