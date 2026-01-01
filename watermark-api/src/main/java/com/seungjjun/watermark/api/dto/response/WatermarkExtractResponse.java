package com.seungjjun.watermark.api.dto.response;

public record WatermarkExtractResponse(
    String extractedText,
    int textLength,
    boolean success
) {

    public static WatermarkExtractResponse from(String extractedText) {
        return new WatermarkExtractResponse(
                extractedText,
                extractedText.length(),
                true
        );
    }
}
