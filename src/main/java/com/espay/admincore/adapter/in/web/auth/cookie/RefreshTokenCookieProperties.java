package com.espay.admincore.adapter.in.web.auth.cookie;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * {@code security.refresh-cookie} 설정을 Refresh Token HTTP 쿠키 어댑터에 제공한다.
 *
 * @param name Refresh Token 쿠키 이름
 * @param secure HTTPS에서만 쿠키를 전송할지 여부
 * @param sameSite 크로스 사이트 쿠키 전송 정책
 * @param path 쿠키를 전송할 인증 API 경로
 */
@ConfigurationProperties("security.refresh-cookie")
public record RefreshTokenCookieProperties(
        String name,
        boolean secure,
        String sameSite,
        String path
) {

    /** 필수 속성과 SameSite=None의 Secure 조건을 검증한다. */
    public RefreshTokenCookieProperties {
        if (name == null || name.isBlank() || sameSite == null || sameSite.isBlank()
                || path == null || path.isBlank()) {
            throw new IllegalArgumentException("Refresh Token 쿠키 설정은 비어 있을 수 없습니다.");
        }
        if ("None".equalsIgnoreCase(sameSite) && !secure) {
            throw new IllegalArgumentException("SameSite=None 쿠키는 Secure 설정이 필요합니다.");
        }
    }
}
