package com.espay.admincore.adapter.out.redis;

import com.espay.admincore.adapter.out.redis.entity.PreAuthStateRedisEntity;
import com.espay.admincore.adapter.out.redis.mapper.PreAuthStateRedisMapper;
import com.espay.admincore.adapter.out.redis.repository.PreAuthStateRedisRepository;
import com.espay.admincore.application.dto.auth.ConsumePreAuthStateCommand;
import com.espay.admincore.application.dto.auth.CreatePreAuthStateCommand;
import com.espay.admincore.application.dto.auth.PreAuthPurpose;
import com.espay.admincore.application.dto.auth.PreAuthState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedisPreAuthStateAdapterTest {
    @Mock
    private PreAuthStateRedisRepository repository;
    @Mock
    private PreAuthStateRedisMapper mapper;
    @InjectMocks
    private RedisPreAuthStateAdapter adapter;

    @Test
    void savesMappedRedisEntity() {
        var command = CreatePreAuthStateCommand.of("jti-1", "10", PreAuthPurpose.OTP_LOGIN, 300);
        var entity = PreAuthStateRedisEntity.create("jti-1", "10", "OTP_LOGIN", 300);
        when(mapper.toEntity(command)).thenReturn(entity);

        adapter.create(command);

        verify(repository).save(entity);
    }

    @Test
    void findsAndMapsRedisEntity() {
        var entity = PreAuthStateRedisEntity.create("jti-1", "10", "OTP_LOGIN", 300);
        var state = new PreAuthState("jti-1", "10", PreAuthPurpose.OTP_LOGIN, 0);
        when(repository.findById("jti-1")).thenReturn(Optional.of(entity));
        when(mapper.toState(entity)).thenReturn(state);

        assertThat(adapter.find("jti-1")).contains(state);
    }

    @Test
    void delegatesAtomicFailureIncrementToRepository() {
        when(repository.incrementFailureAndDeleteAtLimit("jti-1", 5)).thenReturn(3);

        assertThat(adapter.recordFailure("jti-1", 5)).isEqualTo(3);
    }

    @Test
    void delegatesAtomicConsumeToRepository() {
        var command = ConsumePreAuthStateCommand.of("jti-1", "10", PreAuthPurpose.OTP_LOGIN);
        when(repository.consumeIfMatches("jti-1", "10", "OTP_LOGIN")).thenReturn(true);

        assertThat(adapter.consume(command)).isTrue();
    }
}
