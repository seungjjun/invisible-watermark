package com.seungjjun.watermark.service;

import com.seungjjun.watermark.common.exception.CoreException;
import com.seungjjun.watermark.common.exception.ErrorCode;
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

    private static final int MIN_IMAGE_SIZE = 8;
    private static final int BLOCK_SIZE = 8;
    private static final int BITS_PER_BYTE = 8;

    private final DCTWatermark dctWatermark = new DCTWatermark();

    public WatermarkEmbedResult embedWatermark(MultipartFile imageFile, String watermarkText) {
        log.info("embedWatermark called - file: {}, watermark: {}",
            imageFile.getOriginalFilename(), watermarkText);

        if (watermarkText == null || watermarkText.trim().isEmpty()) {
            throw new CoreException(ErrorCode.WATERMARK_TEXT_EMPTY);
        }

        try {
            BufferedImage originalImage = ImageIO.read(imageFile.getInputStream());
            if (originalImage == null) {
                throw new CoreException(ErrorCode.IMAGE_READ_FAILED);
            }

            validateImageSize(originalImage);
            validateImageCapacity(originalImage, watermarkText);

            String format = detectImageFormat(imageFile);
            byte[] bytes = dctWatermark.embedWatermark(originalImage, watermarkText, format);

            log.info("Watermark embedded successfully - result size: {} bytes, format: {}", bytes.length, format);
            return WatermarkEmbedResult.of(bytes, format);

        } catch (CoreException e) {
            throw e;
        } catch (IOException e) {
            throw new CoreException(ErrorCode.WATERMARK_EMBEDDING_FAILED, e);
        } catch (Exception e) {
            throw new CoreException(ErrorCode.IMAGE_PROCESSING_FAILED, e);
        }
    }

    private String detectImageFormat(MultipartFile imageFile) {
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

        throw new CoreException(ErrorCode.INVALID_IMAGE_FORMAT);
    }

    public String extractWatermark(MultipartFile imageFile, int watermarkLength) {
        log.info("extractWatermark called - file: {}, length: {}",
            imageFile.getOriginalFilename(), watermarkLength);

        if (watermarkLength <= 0) {
            throw new CoreException(ErrorCode.WATERMARK_LENGTH_INVALID);
        }

        try {
            BufferedImage watermarkedImage = ImageIO.read(imageFile.getInputStream());
            if (watermarkedImage == null) {
                throw new CoreException(ErrorCode.IMAGE_READ_FAILED);
            }

            validateImageSize(watermarkedImage);
            validateExtractionCapacity(watermarkedImage, watermarkLength);

            String extractedText = dctWatermark.extractWatermark(watermarkedImage, watermarkLength);

            log.info("Watermark extracted successfully: {}", extractedText);
            return extractedText;

        } catch (CoreException e) {
            throw e;
        } catch (IOException e) {
            throw new CoreException(ErrorCode.WATERMARK_EXTRACTION_FAILED, e);
        } catch (Exception e) {
            throw new CoreException(ErrorCode.IMAGE_PROCESSING_FAILED, e);
        }
    }

    private void validateImageSize(BufferedImage image) {
        if (image.getWidth() < MIN_IMAGE_SIZE || image.getHeight() < MIN_IMAGE_SIZE) {
            throw new CoreException(ErrorCode.IMAGE_TOO_SMALL);
        }
    }

    private void validateImageCapacity(BufferedImage image, String watermarkText) {
        int width = (image.getWidth() / BLOCK_SIZE) * BLOCK_SIZE;
        int height = (image.getHeight() / BLOCK_SIZE) * BLOCK_SIZE;
        int totalBlocks = (width / BLOCK_SIZE) * (height / BLOCK_SIZE);

        int requiredBits = watermarkText.getBytes().length * BITS_PER_BYTE;

        if (requiredBits > totalBlocks) {
            throw new CoreException(ErrorCode.WATERMARK_LENGTH_EXCEEDS_CAPACITY);
        }
    }

    private void validateExtractionCapacity(BufferedImage image, int watermarkLength) {
        int width = (image.getWidth() / BLOCK_SIZE) * BLOCK_SIZE;
        int height = (image.getHeight() / BLOCK_SIZE) * BLOCK_SIZE;
        int totalBlocks = (width / BLOCK_SIZE) * (height / BLOCK_SIZE);

        int requiredBits = watermarkLength * BITS_PER_BYTE;

        if (requiredBits > totalBlocks) {
            throw new CoreException(ErrorCode.WATERMARK_LENGTH_EXCEEDS_CAPACITY);
        }
    }
}
