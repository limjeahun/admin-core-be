package com.espay.admincore.adapter.in.security.authentication;

/**
 * 검증된 애플리케이션 인증 결과를 Spring Security principal로 변환하기 위한 입력값.
 *
 * @param userId 인증 사용자 ID
 * @param loginId 로그인 ID
 * @param name 사용자명
 * @param roleId 현재 활성 권한 ID
 */
public record AdminPrincipalData(String userId, String loginId, String name, String roleId) {
}
