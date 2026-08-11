package com.espay.admincore.adapter.out.redis;

import com.espay.admincore.adapter.out.redis.entity.RefreshTokenRedisEntity;
import com.espay.admincore.adapter.out.redis.repository.RefreshTokenRedisRepository;
import com.espay.admincore.application.dto.auth.SaveRefreshTokenCommand;
import com.espay.admincore.application.port.out.auth.RefreshTokenPort;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Repository;

/**
 * Spring Data Redis Repository를 Refresh Token Port에 연결하는 출력 어댑터.
 */
@Repository
@RequiredArgsConstructor
public class RedisRefreshTokenAdapter implements RefreshTokenPort {
    private final RefreshTokenRedisRepository repository;

    /**
     * 사용자별 Refresh Token을 TTL이 포함된 Redis Hash 객체로 저장한다.
     *
     * @param command 사용자, Refresh JWT와 유효시간을 묶은 저장 명령
     */
    @Override
    public void save(SaveRefreshTokenCommand command) {
        repository.save(
                RefreshTokenRedisEntity.create(
                        command.userId(), tokenHash(command.refreshToken()), command.expirationMillis()
                )
        );
    }

    /**
     * 기존 토큰 해시가 저장값과 일치할 때 새 토큰 해시와 TTL로 교체한다.
     *
     * @param userId 인증 사용자 ID
     * @param currentRefreshToken 현재 Refresh Token 원문
     * @param newRefreshToken 새로운 Refresh Token 원문
     * @param expirationMillis 새로운 상태의 유효시간(밀리초)
     * @return 기존 토큰이 일치해 교체했으면 {@code true}
     */
    @Override
    public boolean rotate(String userId, String currentRefreshToken,
                          String newRefreshToken, long expirationMillis) {
        return repository.rotateIfMatches(
                userId,
                tokenHash(currentRefreshToken),
                tokenHash(newRefreshToken),
                expirationMillis
        );
    }

    /**
     * 로그아웃 토큰 해시가 현재 저장값과 일치할 때만 Redis Hash를 삭제한다.
     *
     * @param userId 로그아웃 사용자 ID
     * @param refreshToken 로그아웃 요청의 Refresh Token 원문
     * @return 현재 토큰과 일치해 삭제했으면 {@code true}
     */
    @Override
    public boolean deleteIfMatches(String userId, String refreshToken) {
        return repository.deleteIfMatches(userId, tokenHash(refreshToken));
    }

    /**
     * Redis 유출 시 저장값을 Refresh Token으로 직접 사용할 수 없도록 SHA-256 해시로 변환한다.
     *
     * @param refreshToken 해시할 Refresh Token 원문
     * @return 소문자 16진수 SHA-256 해시
     */
    private String tokenHash(String refreshToken) {
        return DigestUtils.sha256Hex(refreshToken);
    }
}
