package com.espay.admincore.domain.exception;

/**
 * 비활성 관리자 권한으로 로그인 또는 보호 기능 사용을 시도했을 때 발생하는 도메인 예외.
 */
public class InactiveRoleException extends RuntimeException {

    /**
     * 인증에 사용할 수 없는 권한 ID를 포함해 예외를 생성한다.
     *
     * @param roleId 비활성 권한 ID
     */
    public InactiveRoleException(String roleId) {
        super("비활성 권한입니다. roleId=" + roleId);
    }
}
