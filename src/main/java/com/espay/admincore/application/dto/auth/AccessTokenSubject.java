package com.espay.admincore.application.dto.auth;

/**
 * Access Token에 기록할 인증 사용자 식별 정보를 표현한다.
 * JWT 구현 세부사항이나 Spring Security 타입을 애플리케이션 계층에 노출하지 않기 위해 사용한다.
 *
 * @param userId 토큰의 주체가 되는 사용자 ID
 * @param loginId 로그인 ID
 * @param roleId 현재 사용자에게 부여된 권한 ID
 */
public record AccessTokenSubject(String userId, String loginId, String roleId) {
}
