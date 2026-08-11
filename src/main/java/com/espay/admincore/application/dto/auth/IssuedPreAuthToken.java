package com.espay.admincore.application.dto.auth;

/**
 * 비밀번호 인증 이후 OTP 단계에 사용할 사전 인증 토큰의 발급 결과.
 *
 * @param token 클라이언트에 전달할 서명된 사전 인증 JWT
 * @param jti JWT와 서버 측 인증 상태를 연결하는 고유 토큰 식별자
 * @param expiresInSeconds 토큰과 서버 측 상태의 유효시간(초)
 */
public record IssuedPreAuthToken(String token, String jti, long expiresInSeconds) {
}
