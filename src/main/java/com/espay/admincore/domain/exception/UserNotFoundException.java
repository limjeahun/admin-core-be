package com.espay.admincore.domain.exception;

/**
 * 요청한 사용자 ID에 해당하는 관리자 사용자를 찾지 못했을 때 발생하는 도메인 예외.
 */
public class UserNotFoundException extends RuntimeException {

    /**
     * 찾지 못한 사용자 ID를 오류 메시지에 포함해 예외를 생성한다.
     *
     * @param userId 조회에 실패한 사용자 ID
     */
    public UserNotFoundException(String userId) {
        super("사용자를 찾을 수 없습니다. userId=" + userId);
    }
}
