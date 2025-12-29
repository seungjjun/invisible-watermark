package com.seungjjun.watermark.api.controller;

import com.seungjjun.watermark.service.WatermarkService;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
    public ResponseEntity<byte[]> embedWatermark(
            @RequestParam("image") MultipartFile image,
            @RequestParam("watermarkText") String watermarkText) {
        log.info("POST /embed - file: {}, watermarkText: {}", image.getOriginalFilename(), watermarkText);

        byte[] result = watermarkService.embedWatermark(image, watermarkText);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        headers.setContentDispositionFormData("attachment", "watermarked-" + image.getOriginalFilename());

        return ResponseEntity.ok()
                .headers(headers)
                .body(result);
    }

    @PostMapping("/extract")
    public ResponseEntity<String> extractWatermark(
            @RequestParam("image") MultipartFile image,
            @RequestParam("length") int length) {
        log.info("POST /extract - file: {}, length: {}", image.getOriginalFilename(), length);

        String result = watermarkService.extractWatermark(image, length);
        return ResponseEntity.ok("Extracted watermark: " + result);
    }
}
