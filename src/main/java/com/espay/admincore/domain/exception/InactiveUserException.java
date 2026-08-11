package com.espay.admincore.domain.exception;

/**
 * 비활성 관리자 계정으로 로그인을 비롯한 인증 작업을 시도했을 때 발생하는 도메인 예외.
 */
public class InactiveUserException extends RuntimeException {

    /**
     * 인증이 허용되지 않은 사용자 ID를 포함해 예외를 생성한다.
     *
     * @param userId 비활성 사용자 ID
     */
    public InactiveUserException(String userId) {
        super("사용 중지된 계정입니다. userId=" + userId);
    }
}
