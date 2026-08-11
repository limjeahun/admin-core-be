package com.espay.admincore.application.dto.user;

/**
 * 기존 관리자 사용자의 프로필, 권한 및 상태를 수정하는 명령.
 *
 * @param userId 수정할 사용자 ID
 * @param name 변경할 사용자명
 * @param email 변경할 이메일 주소
 * @param phoneNo 변경할 연락처
 * @param deptName 변경할 소속 또는 부서명
 * @param roleId 새로 부여할 권한 ID
 * @param status 변경할 사용자 활성 상태
 */
public record UpdateUserCommand(
        String userId,
        String name,
        String email,
        String phoneNo,
        String deptName,
        String roleId,
        String status
) {
    /**
     * 대상 사용자와 프로필·권한·상태 변경값으로 명령을 생성한다.
     * @param userId 수정할 사용자 ID
     * @param name 변경할 사용자명
     * @param email 변경할 이메일 주소
     * @param phoneNo 변경할 연락처
     * @param deptName 변경할 소속 또는 부서명
     * @param roleId 변경할 권한 ID
     * @param status 변경할 사용자 상태
     * @return 사용자 수정 명령
     */
    public static UpdateUserCommand of(String userId, String name, String email, String phoneNo,
                                       String deptName, String roleId, String status) {
        return new UpdateUserCommand(userId, name, email, phoneNo, deptName, roleId, status);
    }
}
