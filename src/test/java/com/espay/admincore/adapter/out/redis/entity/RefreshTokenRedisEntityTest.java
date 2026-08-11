package com.espay.admincore.adapter.out.redis.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RefreshTokenRedisEntityTest {

    @Test
    void rejectsNonPositiveTtl() {
        assertThatThrownBy(() -> RefreshTokenRedisEntity.create("10", "token-hash", 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Refresh Token TTL은 1밀리초 이상이어야 합니다.");
    }
}
