package com.seungjjun.watermark.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

public record WatermarkEmbedRequest(
    @NotNull(message = "Image file is required")
    MultipartFile image,

    @NotBlank(message = "Watermark text is required")
    @Size(min = 1, max = 128, message = "Watermark text must be between 1 and 128 characters")
    String watermarkText
) {
}
