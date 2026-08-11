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
class RefreshTokenRedisAtomicOperationsImplTest {
    private static final String KEY = "admin-core:refresh:10";

    @Mock
    private StringRedisTemplate redisTemplate;
    @InjectMocks
    private RefreshTokenRedisAtomicOperationsImpl operations;

    @Test
    @SuppressWarnings("unchecked")
    void rotatesWhenStoredHashMatches() {
        when(redisTemplate.execute(
                any(RedisScript.class), eq(List.of(KEY)),
                eq("current-hash"), eq("new-hash"), eq("300000")))
                .thenReturn(1L);

        assertThat(operations.rotateIfMatches(
                "10", "current-hash", "new-hash", 300_000)).isTrue();
    }

    @Test
    @SuppressWarnings("unchecked")
    void rejectsRotationWhenStoredHashDoesNotMatch() {
        when(redisTemplate.execute(
                any(RedisScript.class), eq(List.of(KEY)),
                eq("current-hash"), eq("new-hash"), eq("300000")))
                .thenReturn(0L);

        assertThat(operations.rotateIfMatches(
                "10", "current-hash", "new-hash", 300_000)).isFalse();
    }

    @Test
    @SuppressWarnings("unchecked")
    void deletesWhenStoredHashMatches() {
        when(redisTemplate.execute(
                any(RedisScript.class), eq(List.of(KEY)), eq("token-hash")))
                .thenReturn(1L);

        assertThat(operations.deleteIfMatches("10", "token-hash")).isTrue();
    }

    @Test
    @SuppressWarnings("unchecked")
    void rejectsDeleteWhenStoredHashDoesNotMatch() {
        when(redisTemplate.execute(
                any(RedisScript.class), eq(List.of(KEY)), eq("token-hash")))
                .thenReturn(0L);

        assertThat(operations.deleteIfMatches("10", "token-hash")).isFalse();
    }
}
