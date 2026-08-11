package com.espay.admincore.application.dto.auth;

/**
 * 검증을 마친 사전 인증 JWT에서 추출한 신뢰 가능한 클레임.
 *
 * @param jti Redis의 사전 인증 상태를 조회할 토큰 식별자
 * @param userId 인증 대상 사용자 ID
 * @param purpose 토큰 발급 목적
 */
public record PreAuthClaims(String jti, String userId, PreAuthPurpose purpose) {
}
