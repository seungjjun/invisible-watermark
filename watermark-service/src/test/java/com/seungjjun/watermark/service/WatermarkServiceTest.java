package com.seungjjun.watermark.service;

import com.seungjjun.watermark.common.exception.CoreException;
import com.seungjjun.watermark.common.exception.ErrorCode;
import com.seungjjun.watermark.service.dto.WatermarkEmbedResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WatermarkServiceTest {

    private WatermarkService watermarkService;

    @BeforeEach
    void setUp() {
        watermarkService = new WatermarkService();
    }

    @Nested
    @DisplayName("embedWatermark 메서드")
    class EmbedWatermarkTest {

        @Test
        @DisplayName("워터마크 텍스트가 null이면 WATERMARK_TEXT_EMPTY 예외 발생")
        void embedWatermark_WhenTextIsNull_ThrowsException() {
            // given
            MultipartFile imageFile = createValidImageFile(100, 100);

            // when & then
            assertThatThrownBy(() -> watermarkService.embedWatermark(imageFile, null))
                    .isInstanceOf(CoreException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.WATERMARK_TEXT_EMPTY);
        }

        @Test
        @DisplayName("워터마크 텍스트가 빈 문자열이면 WATERMARK_TEXT_EMPTY 예외 발생")
        void embedWatermark_WhenTextIsEmpty_ThrowsException() {
            // given
            MultipartFile imageFile = createValidImageFile(100, 100);

            // when & then
            assertThatThrownBy(() -> watermarkService.embedWatermark(imageFile, "   "))
                    .isInstanceOf(CoreException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.WATERMARK_TEXT_EMPTY);
        }

        @Test
        @DisplayName("이미지 읽기 실패 시 IMAGE_READ_FAILED 예외 발생")
        void embedWatermark_WhenImageReadFails_ThrowsException() {
            // given
            MultipartFile invalidFile = new MockMultipartFile(
                    "file",
                    "test.jpg",
                    "image/jpeg",
                    "invalid image data".getBytes()
            );

            // when & then
            assertThatThrownBy(() -> watermarkService.embedWatermark(invalidFile, "test"))
                    .isInstanceOf(CoreException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.IMAGE_READ_FAILED);
        }

        @Test
        @DisplayName("이미지 크기가 8x8 미만이면 IMAGE_TOO_SMALL 예외 발생")
        void embedWatermark_WhenImageTooSmall_ThrowsException() {
            // given
            MultipartFile imageFile = createValidImageFile(7, 7);

            // when & then
            assertThatThrownBy(() -> watermarkService.embedWatermark(imageFile, "test"))
                    .isInstanceOf(CoreException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.IMAGE_TOO_SMALL);
        }

        @Test
        @DisplayName("이미지 용량이 워터마크를 담기에 부족하면 WATERMARK_LENGTH_EXCEEDS_CAPACITY 예외 발생")
        void embedWatermark_WhenCapacityExceeded_ThrowsException() {
            // given
            MultipartFile imageFile = createValidImageFile(16, 16); // 2x2 blocks = 4 bits
            String longText = "This is a very long watermark text that exceeds image capacity";

            // when & then
            assertThatThrownBy(() -> watermarkService.embedWatermark(imageFile, longText))
                    .isInstanceOf(CoreException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.WATERMARK_LENGTH_EXCEEDS_CAPACITY);
        }

        @Test
        @DisplayName("지원하지 않는 이미지 포맷이면 INVALID_IMAGE_FORMAT 예외 발생")
        void embedWatermark_WhenUnsupportedFormat_ThrowsException() throws IOException {
            // given
            BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);

            MultipartFile imageFile = new MockMultipartFile(
                    "file",
                    "test.bmp",
                    "image/bmp",
                    baos.toByteArray()
            );

            // when & then
            assertThatThrownBy(() -> watermarkService.embedWatermark(imageFile, "test"))
                    .isInstanceOf(CoreException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_IMAGE_FORMAT);
        }

        @Test
        @DisplayName("정상적인 이미지와 텍스트로 워터마크 삽입 성공")
        void embedWatermark_WithValidInputs_Success() {
            // given
            MultipartFile imageFile = createValidImageFile(100, 100);
            String watermarkText = "test";

            // when
            WatermarkEmbedResult result = watermarkService.embedWatermark(imageFile, watermarkText);

            // then
            assertThat(result).isNotNull();
            assertThat(result.watermarkedImageBytes()).isNotEmpty();
            assertThat(result.format()).isEqualTo("jpg");
        }
    }

    @Nested
    @DisplayName("extractWatermark 메서드")
    class ExtractWatermarkTest {

        @Test
        @DisplayName("워터마크 길이가 0 이하이면 WATERMARK_LENGTH_INVALID 예외 발생")
        void extractWatermark_WhenLengthIsZero_ThrowsException() {
            // given
            MultipartFile imageFile = createValidImageFile(100, 100);

            // when & then
            assertThatThrownBy(() -> watermarkService.extractWatermark(imageFile, 0))
                    .isInstanceOf(CoreException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.WATERMARK_LENGTH_INVALID);
        }

        @Test
        @DisplayName("워터마크 길이가 음수이면 WATERMARK_LENGTH_INVALID 예외 발생")
        void extractWatermark_WhenLengthIsNegative_ThrowsException() {
            // given
            MultipartFile imageFile = createValidImageFile(100, 100);

            // when & then
            assertThatThrownBy(() -> watermarkService.extractWatermark(imageFile, -1))
                    .isInstanceOf(CoreException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.WATERMARK_LENGTH_INVALID);
        }

        @Test
        @DisplayName("이미지 읽기 실패 시 IMAGE_READ_FAILED 예외 발생")
        void extractWatermark_WhenImageReadFails_ThrowsException() {
            // given
            MultipartFile invalidFile = new MockMultipartFile(
                    "file",
                    "test.jpg",
                    "image/jpeg",
                    "invalid image data".getBytes()
            );

            // when & then
            assertThatThrownBy(() -> watermarkService.extractWatermark(invalidFile, 4))
                    .isInstanceOf(CoreException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.IMAGE_READ_FAILED);
        }

        @Test
        @DisplayName("이미지 크기가 8x8 미만이면 IMAGE_TOO_SMALL 예외 발생")
        void extractWatermark_WhenImageTooSmall_ThrowsException() {
            // given
            MultipartFile imageFile = createValidImageFile(7, 7);

            // when & then
            assertThatThrownBy(() -> watermarkService.extractWatermark(imageFile, 4))
                    .isInstanceOf(CoreException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.IMAGE_TOO_SMALL);
        }

        @Test
        @DisplayName("추출할 길이가 이미지 용량 초과 시 WATERMARK_LENGTH_EXCEEDS_CAPACITY 예외 발생")
        void extractWatermark_WhenCapacityExceeded_ThrowsException() {
            // given
            MultipartFile imageFile = createValidImageFile(16, 16); // 2x2 blocks = 4 bits
            int longLength = 100; // 800 bits required

            // when & then
            assertThatThrownBy(() -> watermarkService.extractWatermark(imageFile, longLength))
                    .isInstanceOf(CoreException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.WATERMARK_LENGTH_EXCEEDS_CAPACITY);
        }

        @Test
        @DisplayName("정상적인 이미지와 길이로 워터마크 추출 성공")
        void extractWatermark_WithValidInputs_Success() {
            // given
            MultipartFile imageFile = createValidImageFile(100, 100);
            int watermarkLength = 4;

            // when
            String result = watermarkService.extractWatermark(imageFile, watermarkLength);

            // then
            assertThat(result).isNotNull();
            assertThat(result).hasSize(watermarkLength);
        }
    }

    @Test
    @DisplayName("워터마크 삽입 후 추출 시 동일한 길이의 텍스트 반환")
    void embedAndExtract_ReturnsSameLength() {
        // given
        MultipartFile originalImage = createValidImageFile(200, 200);
        String originalText = "test";

        // when
        WatermarkEmbedResult embedResult = watermarkService.embedWatermark(originalImage, originalText);

        MultipartFile watermarkedImage = new MockMultipartFile(
            "file",
            "watermarked.jpg",
            "image/jpeg",
            embedResult.watermarkedImageBytes()
        );
        String extractedText = watermarkService.extractWatermark(watermarkedImage, originalText.length());

        // then
        assertThat(extractedText).hasSize(originalText.length());
    }

    private MultipartFile createValidImageFile(int width, int height) {
        try {
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "jpg", baos);

            return new MockMultipartFile(
                    "file",
                    "test.jpg",
                    "image/jpeg",
                    baos.toByteArray()
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to create test image", e);
        }
    }
}
