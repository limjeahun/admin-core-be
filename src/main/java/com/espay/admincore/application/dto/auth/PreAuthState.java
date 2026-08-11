package com.espay.admincore.application.dto.auth;

/**
 * 사전 인증 JWT에 대응해 서버 저장소에서 관리하는 인증 상태.
 *
 * @param jti 사전 인증 토큰 식별자
 * @param userId 인증 대상 사용자 ID
 * @param purpose 사전 인증 목적
 * @param failedAttempts 현재까지 누적된 OTP 실패 횟수
 */
public record PreAuthState(String jti, String userId, PreAuthPurpose purpose, int failedAttempts) {
}
