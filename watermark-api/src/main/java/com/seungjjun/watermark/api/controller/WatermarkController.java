package com.seungjjun.watermark.api.controller;

import com.seungjjun.watermark.service.WatermarkService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/v1/watermark")
@RequiredArgsConstructor
public class WatermarkController {

    private final WatermarkService watermarkService;

    @PostMapping("/embed")
    public ResponseEntity<String> embedWatermark(
            @RequestParam("image") MultipartFile image,
            @RequestParam("watermarkText") String watermarkText) {
        byte[] result = watermarkService.embedWatermark(image, watermarkText);
        return ResponseEntity.ok("Watermark embedded successfully. Result size: " + result.length + " bytes");
    }

    @PostMapping("/extract")
    public ResponseEntity<String> extractWatermark(
            @RequestParam("image") MultipartFile image,
            @RequestParam("length") int length) {
        String result = watermarkService.extractWatermark(image, length);
        return ResponseEntity.ok("Extracted watermark: " + result);
    }
}
