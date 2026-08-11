package com.espay.admincore.adapter.out.redis.repository;

import com.espay.admincore.adapter.out.redis.entity.RefreshTokenRedisEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.List;

/**
 * Spring Data Redis Script API로 Refresh Token의 민감한 상태 변경을 원자적으로 실행한다.
 */
@RequiredArgsConstructor
public class RefreshTokenRedisAtomicOperationsImpl implements RefreshTokenRedisAtomicOperations {
    /** 기존 토큰 해시가 일치할 때만 새 해시와 TTL을 함께 적용하는 Script다. */
    private static final RedisScript<Long> ROTATE = new DefaultRedisScript<>("""
            local tokenHash = redis.call('HGET', KEYS[1], 'tokenHash')

            if not tokenHash or tokenHash ~= ARGV[1] then
                return 0
            end

            redis.call('HSET', KEYS[1], 'tokenHash', ARGV[2], 'ttlMillis', ARGV[3])
            redis.call('PEXPIRE', KEYS[1], ARGV[3])
            return 1
            """, Long.class);

    /** 토큰 해시가 일치할 때만 로그인 상태를 삭제하는 Script다. */
    private static final RedisScript<Long> DELETE_IF_MATCHES = new DefaultRedisScript<>("""
            local tokenHash = redis.call('HGET', KEYS[1], 'tokenHash')

            if not tokenHash or tokenHash ~= ARGV[1] then
                return 0
            end

            redis.call('DEL', KEYS[1])
            return 1
            """, Long.class);

    private final StringRedisTemplate redisTemplate;

    /** {@inheritDoc} */
    @Override
    public boolean rotateIfMatches(String userId, String currentTokenHash, String newTokenHash, long ttlMillis) {
        Long result = redisTemplate.execute(
                ROTATE,
                List.of(redisKey(userId)),
                currentTokenHash,
                newTokenHash,
                Long.toString(ttlMillis)
        );
        return Long.valueOf(1L).equals(result);
    }

    /** {@inheritDoc} */
    @Override
    public boolean deleteIfMatches(String userId, String tokenHash) {
        Long result = redisTemplate.execute(
                DELETE_IF_MATCHES,
                List.of(redisKey(userId)),
                tokenHash
        );
        return Long.valueOf(1L).equals(result);
    }

    /**
     * Repository Keyspace와 사용자 ID를 실제 Redis Key 형식으로 조합한다.
     *
     * @param userId 인증 사용자 ID
     * @return Lua Script에 전달할 Redis Key
     */
    private String redisKey(String userId) {
        return RefreshTokenRedisEntity.KEYSPACE + ":" + userId;
    }
}
