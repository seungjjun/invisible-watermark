package com.seungjjun.watermark.api.controller;

import com.seungjjun.watermark.api.dto.request.WatermarkEmbedRequest;
import com.seungjjun.watermark.api.dto.request.WatermarkExtractRequest;
import com.seungjjun.watermark.api.dto.response.WatermarkExtractResponse;
import com.seungjjun.watermark.service.WatermarkService;
import com.seungjjun.watermark.service.dto.WatermarkEmbedResult;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/v1/watermark")
@RequiredArgsConstructor
public class WatermarkController {

    private final WatermarkService watermarkService;

    @PostMapping("/embed")
    public ResponseEntity<byte[]> embedWatermark(@Valid WatermarkEmbedRequest request) {
        log.info("POST /embed - file: {}, watermarkText: {}",
                request.image().getOriginalFilename(),
                request.watermarkText());

        WatermarkEmbedResult result = watermarkService.embedWatermark(
                request.image(),
                request.watermarkText()
        );

        log.info("Watermark embedded successfully - size: {} bytes, format: {}",
                result.watermarkedImageBytes().length, result.format());

        String filename = "watermarked." + result.format();
        return ResponseEntity.ok()
                .contentType(getMediaType(result.format()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(result.watermarkedImageBytes());
    }

    @PostMapping("/extract")
    public WatermarkExtractResponse extractWatermark(@Valid WatermarkExtractRequest request) {
        log.info("POST /extract - file: {}, length: {}",
                request.image().getOriginalFilename(),
                request.watermarkLength());

        String extractedText = watermarkService.extractWatermark(
                request.image(),
                request.watermarkLength()
        );

        log.info("Watermark extracted successfully - text: '{}'", extractedText);
        return WatermarkExtractResponse.from(extractedText);
    }

    private MediaType getMediaType(String format) {
        return switch (format.toLowerCase()) {
            case "png" -> MediaType.IMAGE_PNG;
            case "jpg", "jpeg" -> MediaType.IMAGE_JPEG;
            case "gif" -> MediaType.IMAGE_GIF;
            default -> MediaType.APPLICATION_OCTET_STREAM;
        };
    }
}
