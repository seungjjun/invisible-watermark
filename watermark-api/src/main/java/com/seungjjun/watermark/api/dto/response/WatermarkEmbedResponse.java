package com.seungjjun.watermark.api.dto.response;

import java.util.Base64;

public record WatermarkEmbedResponse(
    String imageData,
    String format,
    long imageSize,
    boolean success,
    String watermarkText

) {

    public static WatermarkEmbedResponse of(byte[] watermarkedImageBytes,
                                            String format,
                                            String watermarkText) {
        String base64Image = Base64.getEncoder().encodeToString(watermarkedImageBytes);
        return new WatermarkEmbedResponse(
                base64Image,
                format,
                watermarkedImageBytes.length,
                true,
                watermarkText
        );
    }
}
