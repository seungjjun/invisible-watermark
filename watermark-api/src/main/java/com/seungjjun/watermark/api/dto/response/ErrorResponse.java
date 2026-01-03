package com.seungjjun.watermark.api.dto.response;

import com.seungjjun.watermark.common.exception.ErrorCode;

public class ErrorResponse {

    private final String code;
    private final String message;
    private final Object data;

    private ErrorResponse(ErrorCode errorCode, String message, Object data) {
        this.code = errorCode.getCode();
        this.message = message;
        this.data = data;
    }

    public static ErrorResponse error(ErrorCode error) {
        return new ErrorResponse(error, error.getMessage(), null);
    }

    public static ErrorResponse error(ErrorCode error, Object data) {
        return new ErrorResponse(error, error.getMessage(), data);
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public Object getData() {
        return data;
    }
}
