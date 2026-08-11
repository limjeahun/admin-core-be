package com.espay.admincore.domain.exception;

/**
 * 요청한 권한 ID에 해당하는 관리자 권한을 찾지 못했을 때 발생하는 도메인 예외.
 */
public class RoleNotFoundException extends RuntimeException {

    /**
     * 찾지 못한 권한 ID를 오류 메시지에 포함해 예외를 생성한다.
     *
     * @param roleId 조회에 실패한 권한 ID
     */
    public RoleNotFoundException(String roleId) {
        super("권한을 찾을 수 없습니다. roleId=" + roleId);
    }
}
