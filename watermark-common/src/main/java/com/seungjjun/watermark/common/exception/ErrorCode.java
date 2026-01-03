package com.seungjjun.watermark.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.slf4j.event.Level;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    INVALID_IMAGE_FORMAT("WM001", "지원하지 않는 이미지 형식입니다", 400, Level.WARN),
    INVALID_IMAGE_FILE("WM002", "유효하지 않은 이미지 파일입니다", 400, Level.WARN),
    IMAGE_TOO_SMALL("WM003", "이미지 크기가 너무 작아 워터마크를 삽입할 수 없습니다", 400, Level.INFO),
    IMAGE_READ_FAILED("WM004", "이미지 파일을 읽을 수 없습니다", 400, Level.WARN),

    WATERMARK_TEXT_EMPTY("WM101", "워터마크 텍스트가 비어있습니다", 400, Level.INFO),
    WATERMARK_LENGTH_INVALID("WM103", "워터마크 길이가 유효하지 않습니다", 400, Level.INFO),
    WATERMARK_LENGTH_EXCEEDS_CAPACITY("WM104", "이미지 용량을 초과하는 워터마크 길이입니다", 400, Level.INFO),

    WATERMARK_EMBEDDING_FAILED("WM201", "워터마크 삽입 중 오류가 발생했습니다", 500, Level.ERROR),
    WATERMARK_EXTRACTION_FAILED("WM202", "워터마크 추출 중 오류가 발생했습니다", 500, Level.ERROR),
    IMAGE_PROCESSING_FAILED("WM203", "이미지 처리 중 오류가 발생했습니다", 500, Level.ERROR),
    IMAGE_CONVERSION_FAILED("WM204", "이미지 변환 중 오류가 발생했습니다", 500, Level.ERROR),

    INTERNAL_SERVER_ERROR("WM999", "예기치 못한 오류가 발생했습니다", 500, Level.ERROR);

    private final String code;
    private final String message;
    private final int httpStatus;
    private final Level logLevel;
}
