package com.espay.admincore.application.dto.auth;

/**
 * 아이디와 비밀번호로 이루어진 1차 인증의 성공 결과.
 *
 * @param preAuthToken OTP 등록 또는 검증에 사용할 사전 인증 토큰
 * @param expiresInSeconds 사전 인증 토큰의 남은 유효시간(초)
 * @param loginId 인증된 사용자의 로그인 ID
 * @param name 인증된 사용자명
 * @param otpRegistered OTP 비밀키가 이미 등록되어 있는지 여부
 */
public record LoginChallengeResult(
        String preAuthToken,
        long expiresInSeconds,
        String loginId,
        String name,
        boolean otpRegistered
) {
}
