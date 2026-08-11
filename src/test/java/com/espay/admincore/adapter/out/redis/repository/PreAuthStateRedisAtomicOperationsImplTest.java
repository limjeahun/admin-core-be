package com.espay.admincore.adapter.out.redis.repository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PreAuthStateRedisAtomicOperationsImplTest {
    private static final String KEY = "admin-core:pre-auth:jti-1";

    @Mock
    private StringRedisTemplate redisTemplate;
    @InjectMocks
    private PreAuthStateRedisAtomicOperationsImpl operations;

    @Test
    @SuppressWarnings("unchecked")
    void returnsIncrementedFailureCountFromScript() {
        when(redisTemplate.execute(any(RedisScript.class), eq(List.of(KEY)), eq("5")))
                .thenReturn(3L);

        assertThat(operations.incrementFailureAndDeleteAtLimit("jti-1", 5)).isEqualTo(3);
    }

    @Test
    @SuppressWarnings("unchecked")
    void reportsMissingStateWhenFailureScriptReturnsNull() {
        when(redisTemplate.execute(any(RedisScript.class), eq(List.of(KEY)), eq("5")))
                .thenReturn(null);

        assertThat(operations.incrementFailureAndDeleteAtLimit("jti-1", 5)).isEqualTo(-1);
    }

    @Test
    @SuppressWarnings("unchecked")
    void consumesMatchingStateWhenScriptReturnsOne() {
        when(redisTemplate.execute(any(RedisScript.class), eq(List.of(KEY)), eq("10"), eq("OTP_LOGIN")))
                .thenReturn(1L);

        assertThat(operations.consumeIfMatches("jti-1", "10", "OTP_LOGIN")).isTrue();
    }

    @Test
    @SuppressWarnings("unchecked")
    void rejectsStateWhenConsumeScriptDoesNotReturnOne() {
        when(redisTemplate.execute(any(RedisScript.class), eq(List.of(KEY)), eq("10"), eq("OTP_LOGIN")))
                .thenReturn(0L);

        assertThat(operations.consumeIfMatches("jti-1", "10", "OTP_LOGIN")).isFalse();
    }
}
