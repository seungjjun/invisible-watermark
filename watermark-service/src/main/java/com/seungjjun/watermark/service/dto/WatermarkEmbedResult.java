package com.seungjjun.watermark.service.dto;

public record WatermarkEmbedResult(
    byte[] watermarkedImageBytes,
    String format
) {

    public static WatermarkEmbedResult of(byte[] bytes, String format) {
        return new WatermarkEmbedResult(bytes, format);
    }
}
