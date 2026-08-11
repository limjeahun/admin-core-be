package com.espay.admincore.adapter.out.redis.mapper;

import com.espay.admincore.application.dto.auth.CreatePreAuthStateCommand;
import com.espay.admincore.application.dto.auth.PreAuthPurpose;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PreAuthStateRedisMapperTest {
    private final PreAuthStateRedisMapper mapper = new PreAuthStateRedisMapper();

    @Test
    void mapsCreateCommandToRedisEntityWithInitialStateAndTtl() {
        var command = CreatePreAuthStateCommand.of("jti-1", "10", PreAuthPurpose.OTP_LOGIN, 300);

        var entity = mapper.toEntity(command);

        assertThat(entity.getJti()).isEqualTo("jti-1");
        assertThat(entity.getUserId()).isEqualTo("10");
        assertThat(entity.getPurpose()).isEqualTo("OTP_LOGIN");
        assertThat(entity.getFailedAttempts()).isZero();
        assertThat(entity.getTtlSeconds()).isEqualTo(300);
    }

    @Test
    void mapsRedisEntityToApplicationState() {
        var entity = new com.espay.admincore.adapter.out.redis.entity.PreAuthStateRedisEntity(
                "jti-1", "10", "OTP_LOGIN", 2, 180);

        var state = mapper.toState(entity);

        assertThat(state.jti()).isEqualTo("jti-1");
        assertThat(state.userId()).isEqualTo("10");
        assertThat(state.purpose()).isEqualTo(PreAuthPurpose.OTP_LOGIN);
        assertThat(state.failedAttempts()).isEqualTo(2);
    }
}
