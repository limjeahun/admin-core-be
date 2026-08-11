package com.espay.admincore.application.dto.user;

import com.espay.admincore.domain.model.role.AdminRole;
import com.espay.admincore.domain.model.user.AdminUser;

import java.time.LocalDateTime;

/**
 * 관리자 사용자와 권한 정보를 조합한 조회 결과.
 *
 * @param userId 사용자 ID
 * @param loginId 로그인 ID
 * @param name 사용자명
 * @param email 이메일 주소
 * @param phoneNo 연락처
 * @param deptName 소속 또는 부서명
 * @param roleId 부여된 권한 ID
 * @param roleName 부여된 권한명
 * @param status 사용자 활성 상태
 * @param otpRegistered OTP 비밀키 등록 여부
 * @param lastLoginAt 최종 OTP 로그인 완료 시각
 * @param createdAt 생성 시각
 * @param updatedAt 최종 수정 시각
 */
public record UserResult(
        String userId,
        String loginId,
        String name,
        String email,
        String phoneNo,
        String deptName,
        String roleId,
        String roleName,
        String status,
        boolean otpRegistered,
        LocalDateTime lastLoginAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    /**
     * 사용자와 권한 도메인 모델을 화면 조회 결과로 조합한다.
     *
     * @param user 변환할 사용자 도메인 모델
     * @param role 사용자에게 부여된 권한, 조회할 수 없으면 {@code null}
     * @return 권한명과 OTP 등록 여부가 포함된 사용자 결과
     */
    public static UserResult from(AdminUser user, AdminRole role) {
        return new UserResult(user.getId(), user.getLoginId(), user.getName(), user.getEmail(), user.getPhoneNo(), user.getDeptName(),
                user.getRoleId(), role == null ? null : role.getName(), user.getStatus().name(), user.hasOtpSecret(),
                user.getLastLoginAt(), user.getCreatedAt(), user.getUpdatedAt());
    }
}
