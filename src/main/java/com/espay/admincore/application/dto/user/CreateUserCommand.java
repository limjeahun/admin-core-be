package com.espay.admincore.application.dto.user;

/**
 * 신규 관리자 사용자를 생성하는 명령.
 *
 * @param loginId 중복될 수 없는 로그인 ID
 * @param name 사용자명
 * @param email 중복될 수 없는 이메일 주소
 * @param phoneNo 연락처
 * @param deptName 소속 또는 부서명
 * @param roleId 부여할 활성 권한 ID
 * @param password BCrypt로 암호화하기 전 원문 비밀번호
 * @param status 사용자 활성 상태 표현값
 */
public record CreateUserCommand(
        String loginId,
        String name,
        String email,
        String phoneNo,
        String deptName,
        String roleId,
        String password,
        String status
) {
    /**
     * 프로필·권한·비밀번호 정보로 사용자 생성 명령을 만든다.
     * @param loginId 로그인 ID
     * @param name 사용자명
     * @param email 이메일 주소
     * @param phoneNo 연락처
     * @param deptName 소속 또는 부서명
     * @param roleId 부여할 권한 ID
     * @param password 원문 비밀번호
     * @param status 사용자 활성 상태
     * @return 사용자 생성 명령
     */
    public static CreateUserCommand of(String loginId, String name, String email, String phoneNo,
                                       String deptName, String roleId, String password, String status) {
        return new CreateUserCommand(loginId, name, email, phoneNo, deptName, roleId, password, status);
    }
}
