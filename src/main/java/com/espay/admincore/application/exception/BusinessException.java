package com.espay.admincore.application.exception;

import lombok.Getter;

/**
 * 애플리케이션 유스케이스에서 예상 가능한 정책 위반을 표현하는 프레임워크 독립 런타임 예외.
 */
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    /**
     * 지정한 애플리케이션 오류 분류와 상세 메시지로 예외를 생성한다.
     *
     * @param errorCode 프레임워크 독립 오류 분류
     * @param message 실패 원인을 설명하는 상세 메시지
     */
    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
