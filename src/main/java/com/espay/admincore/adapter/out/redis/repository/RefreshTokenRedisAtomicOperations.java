package com.espay.admincore.adapter.out.redis.repository;

/**
 * Refresh Token Redis Hash의 비교·교체 및 비교·삭제를 원자적으로 처리하는 Repository Fragment.
 */
public interface RefreshTokenRedisAtomicOperations {

    /**
     * 현재 해시가 기대값과 일치할 때 새로운 해시와 TTL로 교체한다.
     *
     * @param userId 인증 사용자 ID
     * @param currentTokenHash 클라이언트가 제시한 현재 토큰의 SHA-256 해시
     * @param newTokenHash 새로 발급한 토큰의 SHA-256 해시
     * @param ttlMillis 새로운 Redis Key 유효시간(밀리초)
     * @return 현재 해시가 일치해 교체했으면 {@code true}
     */
    boolean rotateIfMatches(String userId, String currentTokenHash, String newTokenHash, long ttlMillis);

    /**
     * 현재 해시가 기대값과 일치할 때 Refresh Token 상태를 삭제한다.
     *
     * @param userId 인증 사용자 ID
     * @param tokenHash 로그아웃 요청 토큰의 SHA-256 해시
     * @return 현재 해시가 일치해 삭제했으면 {@code true}
     */
    boolean deleteIfMatches(String userId, String tokenHash);
}
