package com.espay.admincore.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * {@code security.trusted-proxy} 설정을 클라이언트 IP 해석 로직에 제공한다.
 * 활성화된 환경에서만 프록시가 전달한 {@code X-Forwarded-For}/{@code X-Real-IP} 헤더를 신뢰한다.
 *
 * @param enabled 신뢰 프록시 헤더 사용 여부
 */
@ConfigurationProperties("security.trusted-proxy")
public record TrustedProxyProperties(boolean enabled) {
}
