package com.espay.admincore.adapter.in.web.auth.cookie;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RefreshTokenCookiePropertiesTest {

    @Test
    void rejectsBlankRequiredProperty() {
        assertThatThrownBy(() -> new RefreshTokenCookieProperties(
                "", true, "Strict", "/api/v1/auth"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Refresh Token 쿠키 설정은 비어 있을 수 없습니다.");
    }

    @Test
    void rejectsInsecureSameSiteNoneCookie() {
        assertThatThrownBy(() -> new RefreshTokenCookieProperties(
                "admin_refresh_token", false, "None", "/api/v1/auth"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("SameSite=None 쿠키는 Secure 설정이 필요합니다.");
    }
}
