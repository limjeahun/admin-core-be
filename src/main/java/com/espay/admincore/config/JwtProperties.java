package com.espay.admincore.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * {@code security.jwt} 설정을 타입 안전하게 제공하는 JWT 구성 속성.
 * 서명 키와 발급자뿐 아니라 액세스·리프레시·사전 인증 토큰의 유효 시간을 한곳에서 관리한다.
 *
 * @param secret 토큰 서명과 검증에 사용하는 비밀 키
 * @param issuer 토큰의 발급자({@code iss}) 값
 * @param accessTokenExpiration 액세스 토큰 유효 시간
 * @param refreshTokenExpiration 리프레시 토큰 유효 시간
 * @param preAuthExpiration OTP 완료 전 사전 인증 토큰 유효 시간
 */
@ConfigurationProperties("security.jwt")
public record JwtProperties(
        String secret,
        String issuer,
        Duration accessTokenExpiration,
        Duration refreshTokenExpiration,
        Duration preAuthExpiration
) {
}
