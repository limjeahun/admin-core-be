package com.espay.admincore.application.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 예상 가능한 애플리케이션 실패를 프레임워크 독립적인 안정적 코드로 구분한다.
 * HTTP 상태 매핑은 입력 어댑터가 담당한다.
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    /** 요청 값의 형식 또는 업무 입력 조건이 올바르지 않음. */
    INVALID_INPUT("INVALID_INPUT"),
    /** 로그인 ID 또는 비밀번호가 일치하지 않음. */
    INVALID_CREDENTIALS("INVALID_CREDENTIALS"),
    /** 유효한 인증 정보가 없어 요청자를 확인할 수 없음. */
    UNAUTHORIZED("UNAUTHORIZED"),
    /** 전달된 인증 토큰의 서명 또는 클레임이 유효하지 않음. */
    TOKEN_INVALID("TOKEN_INVALID"),
    /** 전달된 인증 토큰의 유효 시간이 만료됨. */
    TOKEN_EXPIRED("TOKEN_EXPIRED"),
    /** 비활성화 또는 잠금 상태의 계정으로 접근함. */
    ACCOUNT_LOCKED("ACCOUNT_LOCKED"),
    /** 인증 사용자가 요청 기능을 수행할 권한이 없음. */
    ACCESS_DENIED("ACCESS_DENIED"),
    /** 요청한 사용자 식별자에 해당하는 사용자가 없음. */
    USER_NOT_FOUND("USER_NOT_FOUND"),
    /** 요청한 권한 식별자에 해당하는 권한이 없음. */
    ROLE_NOT_FOUND("ROLE_NOT_FOUND"),
    /** 동일한 사용자 고유 정보가 이미 존재함. */
    USER_DUPLICATE("USER_DUPLICATE"),
    /** 동일한 권한 이름이 이미 존재함. */
    ROLE_DUPLICATE("ROLE_DUPLICATE"),
    /** 예상하지 못한 서버 내부 처리 실패가 발생함. */
    INTERNAL_ERROR("INTERNAL_ERROR");

    private final String code;
}
