package com.seungjjun.watermark.service;

import com.seungjjun.watermark.core.dct.DCTWatermark;
import com.seungjjun.watermark.service.dto.WatermarkEmbedResult;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class WatermarkService {

    private static final String FORMAT_JPG = "jpg";
    private static final String FORMAT_PNG = "png";
    private static final String FORMAT_JPEG = "jpeg";
    private static final String EXT_JPG = ".jpg";
    private static final String EXT_JPEG = ".jpeg";
    private static final String EXT_PNG = ".png";

    private final DCTWatermark dctWatermark = new DCTWatermark();

    public WatermarkEmbedResult embedWatermark(MultipartFile imageFile, String watermarkText) {
        log.info("embedWatermark called - file: {}, watermark: {}",
            imageFile.getOriginalFilename(), watermarkText);

        try {
            BufferedImage originalImage = ImageIO.read(imageFile.getInputStream());
            if (originalImage == null) {
                throw new IllegalArgumentException("Invalid image file");
            }

            String format = detectImageFormat(imageFile);
            byte[] bytes = dctWatermark.embedWatermark(originalImage, watermarkText, format);

            log.info("Watermark embedded successfully - result size: {} bytes, format: {}", bytes.length, format);
            return WatermarkEmbedResult.of(bytes, format);

        } catch (IOException e) {
            log.error("Failed to embed watermark", e);
            throw new RuntimeException("Failed to embed watermark: " + e.getMessage(), e);
        }
    }

    private String detectImageFormat(MultipartFile imageFile) throws IOException {
        String contentType = imageFile.getContentType();
        if (contentType != null) {
            if (contentType.contains(FORMAT_JPEG) || contentType.contains(FORMAT_JPG)) {
                return FORMAT_JPG;
            } else if (contentType.contains(FORMAT_PNG)) {
                return FORMAT_PNG;
            }
        }

        String filename = imageFile.getOriginalFilename();
        if (filename != null) {
            String lowerFilename = filename.toLowerCase();
            if (lowerFilename.endsWith(EXT_JPG) || lowerFilename.endsWith(EXT_JPEG)) {
                return FORMAT_JPG;
            } else if (lowerFilename.endsWith(EXT_PNG)) {
                return FORMAT_PNG;
            }
        }

        return FORMAT_JPG;
    }

    public String extractWatermark(MultipartFile imageFile, int watermarkLength) {
        log.info("extractWatermark called - file: {}, length: {}",
            imageFile.getOriginalFilename(), watermarkLength);

        try {
            BufferedImage watermarkedImage = ImageIO.read(imageFile.getInputStream());
            if (watermarkedImage == null) {
                throw new IllegalArgumentException("Invalid image file");
            }

            String extractedText = dctWatermark.extractWatermark(watermarkedImage, watermarkLength);

            log.info("Watermark extracted successfully: {}", extractedText);
            return extractedText;

        } catch (IOException e) {
            log.error("Failed to extract watermark", e);
            throw new RuntimeException("Failed to extract watermark: " + e.getMessage(), e);
        }
    }
}
