package com.espay.admincore.adapter.out.security.jwt;

import com.espay.admincore.application.dto.auth.AccessTokenSubject;
import com.espay.admincore.config.JwtProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenAdapterTest {
    JwtTokenAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new JwtTokenAdapter(new JwtProperties(
                "test-jwt-secret-key-minimum-32-characters-long",
                "test", Duration.ofMinutes(30), Duration.ofDays(7), Duration.ofMinutes(5)));
    }

    @Test
    void distinguishesAccessAndRefreshTokens() {
        AccessTokenSubject subject = new AccessTokenSubject("1", "admin01", "1");

        String access = adapter.issueAccessToken(subject);
        String refresh = adapter.issueRefreshToken("1");

        assertThat(adapter.isValid(access, "ACCESS")).isTrue();
        assertThat(adapter.isValid(access, "REFRESH")).isFalse();
        assertThat(adapter.isValid(refresh, "REFRESH")).isTrue();
        assertThat(adapter.getUserId(refresh)).isEqualTo("1");
    }
}
