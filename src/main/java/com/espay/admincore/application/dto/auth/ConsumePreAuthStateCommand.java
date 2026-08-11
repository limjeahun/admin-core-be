package com.espay.admincore.application.dto.auth;

/**
 * 검증이 완료된 사전 인증 상태의 일회성 소비를 요청하는 명령.
 *
 * @param jti 사전 인증 토큰 고유 식별자
 * @param userId 인증 대상 사용자 ID
 * @param purpose 사전 인증 상태의 사용 목적
 */
public record ConsumePreAuthStateCommand(String jti, String userId, PreAuthPurpose purpose) {
    /**
     * 토큰과 사용자·목적으로 명령을 생성한다.
     * @param jti 사전 인증 토큰 고유 식별자
     * @param userId 인증 대상 사용자 ID
     * @param purpose 사전 인증 상태의 사용 목적
     * @return 사전 인증 상태 소비 명령
     */
    public static ConsumePreAuthStateCommand of(String jti, String userId, PreAuthPurpose purpose) {
        return new ConsumePreAuthStateCommand(jti, userId, purpose);
    }
}
