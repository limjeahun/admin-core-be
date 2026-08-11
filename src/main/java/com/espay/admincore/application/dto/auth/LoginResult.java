package com.espay.admincore.application.dto.auth;

/**
 * OTP 인증 또는 토큰 갱신을 완료한 사용자의 최종 로그인 결과.
 *
 * @param accessToken 보호된 API 호출에 사용할 Access JWT
 * @param refreshToken HTTP 어댑터가 HttpOnly 쿠키로 전달할 Refresh JWT
 * @param tokenType Authorization 헤더에서 사용할 토큰 유형
 * @param expiresInSeconds Access Token의 유효시간(초)
 * @param refreshTokenExpiresInMillis Refresh Token과 쿠키의 유효시간(밀리초)
 * @param userId 인증된 사용자 ID
 * @param userName 인증된 사용자명
 * @param roleId 현재 사용자에게 부여된 권한 ID
 * @param roleName 현재 사용자에게 부여된 권한명
 */
public record LoginResult(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresInSeconds,
        long refreshTokenExpiresInMillis,
        String userId,
        String userName,
        String roleId,
        String roleName
) {
}
