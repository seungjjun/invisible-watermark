package com.seungjjun.watermark.api.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

public record WatermarkExtractRequest(
    @NotNull(message = "Image file is required")
    MultipartFile image,

    @NotNull(message = "Watermark length is required")
    @Min(value = 1, message = "Watermark length must be at least 1")
    @Max(value = 128, message = "Watermark length must not exceed 128")
    Integer watermarkLength
) {
}
