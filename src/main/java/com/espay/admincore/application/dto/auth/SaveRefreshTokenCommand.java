package com.espay.admincore.application.dto.auth;

/**
 * 사용자별 Refresh Token 상태 저장을 요청하는 명령.
 *
 * @param userId 인증 사용자 ID
 * @param refreshToken 저장할 Refresh Token
 * @param expirationMillis 상태 유지시간(밀리초)
 */
public record SaveRefreshTokenCommand(String userId, String refreshToken, long expirationMillis) {
    /**
     * 사용자와 토큰·유효시간으로 저장 명령을 생성한다.
     * @param userId 인증 사용자 ID
     * @param refreshToken 저장할 Refresh Token
     * @param expirationMillis 상태 유지시간(밀리초)
     * @return Refresh Token 저장 명령
     */
    public static SaveRefreshTokenCommand of(String userId, String refreshToken, long expirationMillis) {
        return new SaveRefreshTokenCommand(userId, refreshToken, expirationMillis);
    }
}
