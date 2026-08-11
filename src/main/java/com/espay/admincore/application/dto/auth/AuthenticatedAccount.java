package com.espay.admincore.application.dto.auth;

/**
 * Access Token 검증 후 현재 DB 상태까지 확인된 인증 계정 정보.
 * 입력 어댑터는 이 값을 Spring Security 전용 principal로 변환한다.
 *
 * @param userId 인증 사용자 ID
 * @param loginId 로그인 ID
 * @param name 사용자명
 * @param roleId 활성 권한 ID
 */
public record AuthenticatedAccount(String userId, String loginId, String name, String roleId) {
}
