package com.espay.admincore.adapter.out.redis.entity;

import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import java.util.concurrent.TimeUnit;

/**
 * 사용자별 현재 유효한 Refresh Token을 저장하는 Redis Hash 객체.
 */
@Getter
@RedisHash(RefreshTokenRedisEntity.KEYSPACE)
public class RefreshTokenRedisEntity {
    /** Refresh Token Redis Key 앞에 사용하는 Keyspace다. */
    public static final String KEYSPACE = "admin-core:refresh";

    /** 인증 사용자 ID이며 Redis Hash의 식별자로 사용한다. */
    @Id
    private final String userId;

    /** 토큰 갱신 요청에서 제시된 토큰의 해시와 비교할 SHA-256 해시다. */
    private final String tokenHash;

    /** Redis Key가 자동 만료될 때까지 남은 시간이며 밀리초 단위를 사용한다. */
    @TimeToLive(unit = TimeUnit.MILLISECONDS)
    private final long ttlMillis;

    /**
     * Spring Data Redis가 신규 객체를 저장하고 기존 Hash를 복원할 때 사용하는 생성자다.
     *
     * @param userId 인증 사용자 ID
     * @param tokenHash 저장할 Refresh Token SHA-256 해시
     * @param ttlMillis Redis Key의 남은 유효시간(밀리초)
     */
    @PersistenceCreator
    public RefreshTokenRedisEntity(String userId, String tokenHash, long ttlMillis) {
        this.userId = userId;
        this.tokenHash = tokenHash;
        this.ttlMillis = ttlMillis;
    }

    /**
     * JWT 만료시간과 동일한 TTL을 가진 Refresh Token 저장 객체를 생성한다.
     *
     * @param userId 인증 사용자 ID
     * @param tokenHash 저장할 Refresh Token SHA-256 해시
     * @param ttlMillis Redis Key의 유효시간(밀리초)
     * @return Redis에 저장할 Refresh Token 객체
     * @throws IllegalArgumentException TTL이 1밀리초보다 작은 경우
     */
    public static RefreshTokenRedisEntity create(String userId, String tokenHash, long ttlMillis) {
        if (ttlMillis < 1) {
            throw new IllegalArgumentException("Refresh Token TTL은 1밀리초 이상이어야 합니다.");
        }
        return new RefreshTokenRedisEntity(userId, tokenHash, ttlMillis);
    }
}
