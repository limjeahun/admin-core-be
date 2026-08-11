package com.espay.admincore.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * {@code security.cors} 설정을 타입 안전하게 제공하는 CORS 구성 속성.
 *
 * @param allowedOrigins 자격 증명 요청을 허용할 정확한 프론트엔드 Origin 목록
 */
@ConfigurationProperties("security.cors")
public record CorsProperties(List<String> allowedOrigins) {

    /** 빈 목록과 자격 증명 요청에 사용할 수 없는 와일드카드 Origin을 거부한다. */
    public CorsProperties {
        allowedOrigins = allowedOrigins == null ? List.of() : List.copyOf(allowedOrigins);
        if (allowedOrigins.isEmpty() || allowedOrigins.stream().anyMatch("*"::equals)) {
            throw new IllegalArgumentException("CORS 허용 Origin은 정확한 주소로 한 개 이상 설정해야 합니다.");
        }
    }
}
