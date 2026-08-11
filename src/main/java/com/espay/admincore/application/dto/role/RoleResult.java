package com.espay.admincore.application.dto.role;

import com.espay.admincore.domain.model.role.AdminRole;

import java.time.LocalDateTime;

/**
 * 관리자 권한 조회 결과와 해당 권한을 사용하는 사용자 수.
 *
 * @param roleId 권한 ID
 * @param name 권한명
 * @param description 권한 설명
 * @param useYn 사용 여부(Y/N)
 * @param userCount 권한이 부여된 사용자 수
 * @param createdAt 생성 시각
 * @param updatedAt 최종 수정 시각
 */
public record RoleResult(
        String roleId,
        String name,
        String description,
        String useYn,
        long userCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    /**
     * 권한 도메인 모델과 사용자 수를 조회 결과로 변환한다.
     *
     * @param role 변환할 권한 도메인 모델
     * @param userCount 권한이 부여된 사용자 수
     * @return 화면에 반환할 권한 결과
     */
    public static RoleResult from(AdminRole role, long userCount) {
        return new RoleResult(role.getId(), role.getName(), role.getDescription(),
                role.isActive() ? "Y" : "N", userCount, role.getCreatedAt(), role.getUpdatedAt());
    }
}
