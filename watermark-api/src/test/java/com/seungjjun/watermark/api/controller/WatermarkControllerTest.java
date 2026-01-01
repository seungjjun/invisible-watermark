package com.seungjjun.watermark.api.controller;

import com.seungjjun.watermark.service.WatermarkService;
import com.seungjjun.watermark.service.dto.WatermarkEmbedResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WatermarkController.class)
@DisplayName("WatermarkController 테스트")
class WatermarkControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WatermarkService watermarkService;

    @Test
    @DisplayName("워터마크 삽입 - 정상 케이스")
    void embedWatermark_Success() throws Exception {
        // given
        MockMultipartFile imageFile = new MockMultipartFile(
                "image",
                "test.png",
                MediaType.IMAGE_PNG_VALUE,
                "test image content".getBytes()
        );

        String watermarkText = "MyWatermark";
        byte[] resultBytes = "watermarked image".getBytes();

        WatermarkEmbedResult mockResult = WatermarkEmbedResult.of(resultBytes, "png");
        given(watermarkService.embedWatermark(any(), eq(watermarkText)))
                .willReturn(mockResult);

        // when & then
        mockMvc.perform(multipart("/v1/watermark/embed")
                        .file(imageFile)
                        .param("watermarkText", watermarkText))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", MediaType.IMAGE_PNG_VALUE))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"watermarked.png\""))
                .andExpect(content().bytes(resultBytes));
    }

    @Test
    @DisplayName("워터마크 삽입 - 이미지 파일 누락")
    void embedWatermark_MissingImage() throws Exception {
        // when & then
        mockMvc.perform(multipart("/v1/watermark/embed")
                        .param("watermarkText", "MyWatermark"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("워터마크 삽입 - 워터마크 텍스트 누락")
    void embedWatermark_MissingWatermarkText() throws Exception {
        // given
        MockMultipartFile imageFile = new MockMultipartFile(
                "image",
                "test.png",
                MediaType.IMAGE_PNG_VALUE,
                "test image content".getBytes()
        );

        // when & then
        mockMvc.perform(multipart("/v1/watermark/embed")
                        .file(imageFile))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("워터마크 삽입 - 워터마크 텍스트가 빈 문자열")
    void embedWatermark_EmptyWatermarkText() throws Exception {
        // given
        MockMultipartFile imageFile = new MockMultipartFile(
                "image",
                "test.png",
                MediaType.IMAGE_PNG_VALUE,
                "test image content".getBytes()
        );

        // when & then
        mockMvc.perform(multipart("/v1/watermark/embed")
                        .file(imageFile)
                        .param("watermarkText", ""))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("워터마크 삽입 - 워터마크 텍스트 길이 초과 (128자 초과)")
    void embedWatermark_WatermarkTextTooLong() throws Exception {
        // given
        MockMultipartFile imageFile = new MockMultipartFile(
                "image",
                "test.png",
                MediaType.IMAGE_PNG_VALUE,
                "test image content".getBytes()
        );

        String longText = "a".repeat(129); // 129자

        // when & then
        mockMvc.perform(multipart("/v1/watermark/embed")
                        .file(imageFile)
                        .param("watermarkText", longText))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("워터마크 추출 - 정상 케이스")
    void extractWatermark_Success() throws Exception {
        // given
        MockMultipartFile imageFile = new MockMultipartFile(
                "image",
                "watermarked.png",
                MediaType.IMAGE_PNG_VALUE,
                "watermarked image content".getBytes()
        );

        int watermarkLength = 11;
        String extractedText = "MyWatermark";

        given(watermarkService.extractWatermark(any(), eq(watermarkLength)))
                .willReturn(extractedText);

        // when & then
        mockMvc.perform(multipart("/v1/watermark/extract")
                        .file(imageFile)
                        .param("watermarkLength", String.valueOf(watermarkLength)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.extractedText").value(extractedText))
                .andExpect(jsonPath("$.textLength").value(extractedText.length()))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("워터마크 추출 - 이미지 파일 누락")
    void extractWatermark_MissingImage() throws Exception {
        // when & then
        mockMvc.perform(multipart("/v1/watermark/extract")
                        .param("watermarkLength", "11"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("워터마크 추출 - 워터마크 길이 누락")
    void extractWatermark_MissingWatermarkLength() throws Exception {
        // given
        MockMultipartFile imageFile = new MockMultipartFile(
                "image",
                "watermarked.png",
                MediaType.IMAGE_PNG_VALUE,
                "watermarked image content".getBytes()
        );

        // when & then
        mockMvc.perform(multipart("/v1/watermark/extract")
                        .file(imageFile))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("워터마크 추출 - 워터마크 길이가 0")
    void extractWatermark_WatermarkLengthZero() throws Exception {
        // given
        MockMultipartFile imageFile = new MockMultipartFile(
                "image",
                "watermarked.png",
                MediaType.IMAGE_PNG_VALUE,
                "watermarked image content".getBytes()
        );

        // when & then
        mockMvc.perform(multipart("/v1/watermark/extract")
                        .file(imageFile)
                        .param("watermarkLength", "0"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("워터마크 추출 - 워터마크 길이가 128 초과")
    void extractWatermark_WatermarkLengthTooLarge() throws Exception {
        // given
        MockMultipartFile imageFile = new MockMultipartFile(
                "image",
                "watermarked.png",
                MediaType.IMAGE_PNG_VALUE,
                "watermarked image content".getBytes()
        );

        // when & then
        mockMvc.perform(multipart("/v1/watermark/extract")
                        .file(imageFile)
                        .param("watermarkLength", "129"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("워터마크 삽입 - JPG 이미지")
    void embedWatermark_JpgImage() throws Exception {
        // given
        MockMultipartFile imageFile = new MockMultipartFile(
                "image",
                "test.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        String watermarkText = "MyWatermark";
        byte[] resultBytes = "watermarked image".getBytes();

        WatermarkEmbedResult mockResult = WatermarkEmbedResult.of(resultBytes, "jpg");
        given(watermarkService.embedWatermark(any(), eq(watermarkText)))
                .willReturn(mockResult);

        // when & then
        mockMvc.perform(multipart("/v1/watermark/embed")
                        .file(imageFile)
                        .param("watermarkText", watermarkText))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", MediaType.IMAGE_JPEG_VALUE))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"watermarked.jpg\""))
                .andExpect(content().bytes(resultBytes));
    }
}
