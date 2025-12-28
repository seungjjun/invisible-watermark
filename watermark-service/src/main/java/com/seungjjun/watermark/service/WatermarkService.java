package com.seungjjun.watermark.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
public class WatermarkService {

    public byte[] embedWatermark(MultipartFile imageFile, String watermarkText) {
        log.info("embedWatermark called - file: {}, watermark: {}",
            imageFile.getOriginalFilename(), watermarkText);

        // TODO: Implement DCT watermarking
        return new byte[0];
    }

    public String extractWatermark(MultipartFile imageFile, int watermarkLength) {
        log.info("extractWatermark called - file: {}, length: {}",
            imageFile.getOriginalFilename(), watermarkLength);

        // TODO: Implement DCT watermark extraction
        return "STUB_WATERMARK";
    }
}
