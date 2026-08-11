package com.espay.admincore.adapter.out.redis;

import com.espay.admincore.adapter.out.redis.entity.RefreshTokenRedisEntity;
import com.espay.admincore.adapter.out.redis.repository.RefreshTokenRedisRepository;
import com.espay.admincore.application.dto.auth.SaveRefreshTokenCommand;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.apache.commons.codec.digest.DigestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedisRefreshTokenAdapterTest {
    @Mock
    private RefreshTokenRedisRepository repository;
    @InjectMocks
    private RedisRefreshTokenAdapter adapter;

    @Test
    void savesRefreshTokenRedisEntityWithTtl() {
        adapter.save(SaveRefreshTokenCommand.of("10", "refresh-token", 300_000));

        ArgumentCaptor<RefreshTokenRedisEntity> captor = ArgumentCaptor.forClass(RefreshTokenRedisEntity.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo("10");
        assertThat(captor.getValue().getTokenHash()).isEqualTo(DigestUtils.sha256Hex("refresh-token"));
        assertThat(captor.getValue().getTtlMillis()).isEqualTo(300_000);
    }

    @Test
    void rotatesRefreshTokenUsingHashes() {
        String currentHash = DigestUtils.sha256Hex("current-token");
        String newHash = DigestUtils.sha256Hex("new-token");
        when(repository.rotateIfMatches("10", currentHash, newHash, 300_000)).thenReturn(true);

        assertThat(adapter.rotate("10", "current-token", "new-token", 300_000)).isTrue();
    }

    @Test
    void reportsFailedRotation() {
        String currentHash = DigestUtils.sha256Hex("current-token");
        String newHash = DigestUtils.sha256Hex("new-token");
        when(repository.rotateIfMatches("10", currentHash, newHash, 300_000)).thenReturn(false);

        assertThat(adapter.rotate("10", "current-token", "new-token", 300_000)).isFalse();
    }

    @Test
    void deletesOnlyMatchingRefreshToken() {
        String tokenHash = DigestUtils.sha256Hex("refresh-token");
        when(repository.deleteIfMatches("10", tokenHash)).thenReturn(true);

        assertThat(adapter.deleteIfMatches("10", "refresh-token")).isTrue();
    }
}
