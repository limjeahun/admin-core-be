package com.espay.admincore.adapter.out.redis.repository;

import com.espay.admincore.adapter.out.redis.entity.PreAuthStateRedisEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.List;

/**
 * Spring Data Redis Script API로 사전 인증 상태의 복합 연산을 원자적으로 실행하는 Repository Fragment 구현체.
 */
@RequiredArgsConstructor
public class PreAuthStateRedisAtomicOperationsImpl implements PreAuthStateRedisAtomicOperations {
    /** 사용자와 인증 목적이 일치할 때 사전 인증 상태를 한 번만 소비하는 Script다. */
    private static final RedisScript<Long> CONSUME = new DefaultRedisScript<>("""
            local userId = redis.call('HGET', KEYS[1], 'userId')
            local purpose = redis.call('HGET', KEYS[1], 'purpose')

            if not userId or userId ~= ARGV[1] or purpose ~= ARGV[2] then
                return 0
            end

            redis.call('DEL', KEYS[1])
            return 1
            """, Long.class);

    /** OTP 실패 횟수를 증가시키고 최대 횟수에 도달하면 사전 인증 상태를 제거하는 Script다. */
    private static final RedisScript<Long> RECORD_FAILURE = new DefaultRedisScript<>("""
            if redis.call('EXISTS', KEYS[1]) == 0 then
                return -1
            end

            local attempts = redis.call('HINCRBY', KEYS[1], 'failedAttempts', 1)

            if attempts >= tonumber(ARGV[1]) then
                redis.call('DEL', KEYS[1])
            end

            return attempts
            """, Long.class);

    private final StringRedisTemplate redisTemplate;

    /**
     * 실패 횟수 증가와 최대 횟수 도달 시 삭제를 하나의 Redis 연산으로 수행한다.
     *
     * @param jti 사전 인증 JWT의 고유 토큰 ID
     * @param maxAttempts 허용할 최대 OTP 실패 횟수
     * @return 증가된 실패 횟수, 상태가 존재하지 않으면 {@code -1}
     */
    @Override
    public int incrementFailureAndDeleteAtLimit(String jti, int maxAttempts) {
        Long result = redisTemplate.execute(
                RECORD_FAILURE, List.of(redisKey(jti)), Integer.toString(maxAttempts));
        return result == null ? -1 : result.intValue();
    }

    /**
     * 사용자와 목적 검증 후 삭제를 하나의 Redis 연산으로 수행해 상태의 일회성 사용을 보장한다.
     *
     * @param jti 사전 인증 JWT의 고유 토큰 ID
     * @param userId 기대하는 사용자 ID
     * @param purpose 기대하는 사전 인증 목적의 이름
     * @return 상태를 검증하고 삭제했으면 {@code true}
     */
    @Override
    public boolean consumeIfMatches(String jti, String userId, String purpose) {
        Long result = redisTemplate.execute(
                CONSUME, List.of(redisKey(jti)), userId, purpose);
        return Long.valueOf(1L).equals(result);
    }

    /**
     * Repository Keyspace와 토큰 ID를 Spring Data Redis 기본 Key 형식으로 조합한다.
     *
     * @param jti 사전 인증 JWT의 고유 토큰 ID
     * @return Lua Script에 전달할 실제 Redis Key
     */
    private String redisKey(String jti) {
        return PreAuthStateRedisEntity.KEYSPACE + ":" + jti;
    }
}
