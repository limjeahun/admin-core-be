package com.espay.admincore.adapter.in.web.user.response;

import com.espay.admincore.application.dto.user.UserResult;

import java.time.LocalDateTime;

/**
 * 단일 관리자 사용자와 부여된 권한의 HTTP 응답.
 *
 * @param userId 사용자 ID
 * @param loginId 로그인 ID
 * @param name 사용자명
 * @param email 이메일
 * @param phoneNo 연락처
 * @param deptName 소속 또는 부서명
 * @param roleId 권한 ID
 * @param roleName 권한명
 * @param status 활성 상태
 * @param otpRegistered OTP 등록 여부
 * @param lastLoginAt 최종 로그인 시각
 * @param createdAt 생성 시각
 * @param updatedAt 수정 시각
 */
public record UserResponse(String userId, String loginId, String name, String email, String phoneNo,
                           String deptName, String roleId, String roleName, String status,
                           boolean otpRegistered, LocalDateTime lastLoginAt,
                           LocalDateTime createdAt, LocalDateTime updatedAt) {
    /**
     * 애플리케이션 사용자 결과를 HTTP 응답으로 변환한다.
     *
     * @param result 변환할 사용자 결과
     * @return 외부 응답용 사용자 정보
     */
    public static UserResponse from(UserResult result) {
        return new UserResponse(result.userId(), result.loginId(), result.name(), result.email(), result.phoneNo(),
                result.deptName(), result.roleId(), result.roleName(), result.status(), result.otpRegistered(),
                result.lastLoginAt(), result.createdAt(), result.updatedAt());
    }
}
