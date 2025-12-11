package com.seungjjun.watermark.core.lsb.model;

import com.seungjjun.watermark.core.lsb.util.BitManipulator;

import java.nio.charset.StandardCharsets;

public class WatermarkData {

    private final byte[] data;
    private final int length;

    public WatermarkData(String text) {
        this.data = text.getBytes(StandardCharsets.UTF_8);
        this.length = data.length;
    }

    public WatermarkData(byte[] data) {
        this.data = data;
        this.length = data.length;
    }

    // 길이 정보와 데이터를 하나의 바이트 배열로 결합
    public byte[] toBytes() {
        byte[] lengthBytes = BitManipulator.intToBytes(length);
        byte[] combined = new byte[4 + length];

        System.arraycopy(lengthBytes, 0, combined, 0, 4);
        System.arraycopy(data, 0, combined, 4, length);

        return combined;
    }

    public static WatermarkData fromBytes(byte[] bytes) {
        return new WatermarkData(bytes);
    }

    public String toText() {
        return new String(data, StandardCharsets.UTF_8);
    }
}
