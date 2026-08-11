package com.espay.admincore.adapter.in.web.auth.cookie;

import com.espay.admincore.application.dto.auth.LoginResult;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RefreshTokenCookieTest {

    @Test
    void issuesSecureHttpOnlyCookieFromLoginResult() {
        RefreshTokenCookie cookie = new RefreshTokenCookie(properties(true));

        String value = cookie.issue(loginResult()).toString();

        assertThat(value)
                .contains("admin_refresh_token=refresh-token")
                .contains("Max-Age=300")
                .contains("Path=/api/v1/auth")
                .contains("Secure")
                .contains("HttpOnly")
                .contains("SameSite=Strict");
    }

    @Test
    void expiresCookieWithSameSecurityAttributes() {
        RefreshTokenCookie cookie = new RefreshTokenCookie(properties(true));

        String value = cookie.expire().toString();

        assertThat(value)
                .contains("admin_refresh_token=")
                .contains("Max-Age=0")
                .contains("Path=/api/v1/auth")
                .contains("Secure")
                .contains("HttpOnly")
                .contains("SameSite=Strict");
    }

    private RefreshTokenCookieProperties properties(boolean secure) {
        return new RefreshTokenCookieProperties(
                "admin_refresh_token", secure, "Strict", "/api/v1/auth");
    }

    private LoginResult loginResult() {
        return new LoginResult(
                "access-token", "refresh-token", "Bearer", 900L, 300_000L,
                "10", "관리자", "1", "운영자");
    }
}
