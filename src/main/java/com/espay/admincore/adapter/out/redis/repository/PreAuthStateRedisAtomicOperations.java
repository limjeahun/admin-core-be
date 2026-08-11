package com.espay.admincore.adapter.out.redis.repository;

/**
 * 여러 Redis 명령을 하나로 실행해야 하는 사전 인증 상태의 원자적 연산 Fragment.
 */
public interface PreAuthStateRedisAtomicOperations {

    /**
     * OTP 실패 횟수를 원자적으로 증가시키고 최대 횟수에 도달하면 상태를 삭제한다.
     *
     * @param jti 사전 인증 JWT의 고유 토큰 ID
     * @param maxAttempts 허용할 최대 OTP 실패 횟수
     * @return 증가된 실패 횟수, 상태가 존재하지 않으면 {@code -1}
     */
    int incrementFailureAndDeleteAtLimit(String jti, int maxAttempts);

    /**
     * 저장된 사용자와 목적이 기대값과 일치할 때 사전 인증 상태를 원자적으로 삭제한다.
     *
     * @param jti 사전 인증 JWT의 고유 토큰 ID
     * @param userId 기대하는 사용자 ID
     * @param purpose 기대하는 사전 인증 목적의 이름
     * @return 상태를 검증하고 삭제했으면 {@code true}
     */
    boolean consumeIfMatches(String jti, String userId, String purpose);
}
