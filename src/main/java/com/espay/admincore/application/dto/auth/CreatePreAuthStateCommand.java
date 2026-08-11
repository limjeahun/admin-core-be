package com.espay.admincore.application.dto.auth;

/**
 * 사전 인증 토큰에 대응하는 서버 측 상태 생성을 요청하는 명령.
 *
 * @param jti 사전 인증 토큰 고유 식별자
 * @param userId 인증 대상 사용자 ID
 * @param purpose 사전 인증 상태의 사용 목적
 * @param ttlSeconds 상태 유지시간(초)
 */
public record CreatePreAuthStateCommand(String jti, String userId, PreAuthPurpose purpose, long ttlSeconds) {
    /**
     * 토큰과 사용자·목적·유효시간으로 명령을 생성한다.
     * @param jti 사전 인증 토큰 고유 식별자
     * @param userId 인증 대상 사용자 ID
     * @param purpose 사전 인증 상태의 사용 목적
     * @param ttlSeconds 상태 유지시간(초)
     * @return 사전 인증 상태 생성 명령
     */
    public static CreatePreAuthStateCommand of(String jti, String userId, PreAuthPurpose purpose, long ttlSeconds) {
        return new CreatePreAuthStateCommand(jti, userId, purpose, ttlSeconds);
    }
}
