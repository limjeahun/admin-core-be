package com.espay.admincore.adapter.out.redis.entity;

import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import java.util.concurrent.TimeUnit;

/**
 * 비밀번호 인증을 통과한 뒤 OTP 인증을 기다리는 상태를 저장하는 Redis Hash 객체.
 */
@Getter
@RedisHash(PreAuthStateRedisEntity.KEYSPACE)
public class PreAuthStateRedisEntity {
    /** Redis Key 앞에 사용하는 사전 인증 상태 Keyspace다. */
    public static final String KEYSPACE = "admin-core:pre-auth";

    /** 사전 인증 JWT의 고유 토큰 ID이며 Redis Hash의 식별자로 사용한다. */
    @Id
    private final String jti;

    /** OTP 인증을 수행할 관리자 사용자 ID다. */
    private final String userId;

    /** OTP 로그인 등 사전 인증 상태가 발급된 목적의 이름이다. */
    private final String purpose;

    /** 현재까지 누적된 OTP 인증 실패 횟수다. */
    private final int failedAttempts;

    /** Redis Key가 자동 만료될 때까지 남은 시간이며 초 단위를 사용한다. */
    @TimeToLive(unit = TimeUnit.SECONDS)
    private final long ttlSeconds;

    /**
     * Spring Data Redis가 신규 상태를 저장하고 기존 Hash를 복원할 때 사용하는 생성자다.
     *
     * @param jti 사전 인증 JWT의 고유 토큰 ID
     * @param userId OTP 인증 대상 사용자 ID
     * @param purpose 사전 인증 목적의 이름
     * @param failedAttempts 누적 OTP 인증 실패 횟수
     * @param ttlSeconds Redis Key의 남은 유효시간(초)
     */
    @PersistenceCreator
    public PreAuthStateRedisEntity(String jti, String userId, String purpose,
                                   int failedAttempts, long ttlSeconds) {
        this.jti = jti;
        this.userId = userId;
        this.purpose = purpose;
        this.failedAttempts = failedAttempts;
        this.ttlSeconds = ttlSeconds;
    }

    /**
     * 실패 횟수가 0인 신규 OTP 대기 상태를 생성한다.
     *
     * @param jti 사전 인증 JWT의 고유 토큰 ID
     * @param userId OTP 인증 대상 사용자 ID
     * @param purpose 사전 인증 목적의 이름
     * @param ttlSeconds Redis Key의 유효시간(초)
     * @return Redis에 저장할 신규 사전 인증 상태
     */
    public static PreAuthStateRedisEntity create(String jti, String userId, String purpose, long ttlSeconds) {
        return new PreAuthStateRedisEntity(jti, userId, purpose, 0, Math.max(1, ttlSeconds));
    }
}
