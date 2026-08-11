package com.espay.admincore.adapter.in.web.role.response;

import com.espay.admincore.application.dto.role.RoleResult;

import java.time.LocalDateTime;

/**
 * 단일 관리자 권한의 HTTP 응답.
 *
 * @param roleId 권한 ID
 * @param name 권한명
 * @param description 권한 설명
 * @param useYn 사용 여부(Y/N)
 * @param userCount 권한이 부여된 사용자 수
 * @param createdAt 생성 시각
 * @param updatedAt 수정 시각
 */
public record RoleResponse(String roleId, String name, String description, String useYn, long userCount,
                           LocalDateTime createdAt, LocalDateTime updatedAt) {
    /**
     * 애플리케이션 권한 결과를 HTTP 응답으로 변환한다.
     *
     * @param result 변환할 권한 결과
     * @return 외부 응답용 권한 정보
     */
    public static RoleResponse from(RoleResult result) {
        return new RoleResponse(result.roleId(), result.name(), result.description(), result.useYn(),
                result.userCount(), result.createdAt(), result.updatedAt());
    }
}
