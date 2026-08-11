package com.espay.admincore.adapter.out.security.jwt;

import com.espay.admincore.application.port.out.auth.JwtTokenPort;
import com.espay.admincore.application.dto.auth.AccessTokenSubject;
import com.espay.admincore.config.JwtProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

/**
 * HMAC 서명 JWT로 최종 로그인용 Access/Refresh Token을 발급하고 검증하는 어댑터.
 */
@Component
public class JwtTokenAdapter implements JwtTokenPort {
    private final JwtProperties properties;
    private final SecretKey key;

    /**
     * 설정된 비밀키 바이트로 HMAC 서명·검증 키를 초기화한다.
     *
     * @param properties JWT 비밀키, 발급자와 토큰별 만료시간
     */
    public JwtTokenAdapter(JwtProperties properties) {
        this.properties = properties;
        this.key = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 사용자 ID, 로그인 ID, 권한 ID와 ACCESS 타입을 담은 JWT를 발급한다.
     *
     * @param subject 인증된 사용자와 권한 식별 정보
     * @return 서명된 Access JWT
     */
    @Override
    public String issueAccessToken(AccessTokenSubject subject) {
        Instant now = Instant.now();
        return Jwts.builder().issuer(properties.issuer()).subject(subject.userId()).id(UUID.randomUUID().toString())
                .claim("type", "ACCESS").claim("loginId", subject.loginId())
                .claim("roleId", subject.roleId()).issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(properties.accessTokenExpiration())))
                .signWith(key).compact();
    }

    /**
     * 사용자 ID와 REFRESH 타입만 담은 장기 JWT를 발급한다.
     *
     * @param userId 인증 사용자 ID
     * @return 서명된 Refresh JWT
     */
    @Override
    public String issueRefreshToken(String userId) {
        Instant now = Instant.now();
        return Jwts.builder().issuer(properties.issuer()).subject(userId).id(UUID.randomUUID().toString())
                .claim("type", "REFRESH").issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(properties.refreshTokenExpiration())))
                .signWith(key).compact();
    }

    /**
     * 서명, 발급자, 만료시간을 검증하고 type 클레임이 예상값과 같은지 확인한다.
     *
     * @param token 검증할 JWT
     * @param expectedType 기대하는 토큰 종류
     * @return 유효하고 타입이 일치하면 {@code true}
     */
    @Override
    public boolean isValid(String token, String expectedType) {
        try {
            Claims claims = claims(token);
            return expectedType.equals(claims.get("type", String.class));
        } catch (JwtException | IllegalArgumentException exception) {
            return false;
        }
    }

    /**
     * JWT 파싱 결과가 만료 오류인지 구분한다.
     *
     * @param token 확인할 JWT
     * @return 만료된 경우만 {@code true}; 다른 오류는 {@code false}
     */
    @Override
    public boolean isExpired(String token) {
        try {
            claims(token);
            return false;
        } catch (ExpiredJwtException exception) {
            return true;
        } catch (JwtException | IllegalArgumentException exception) {
            return false;
        }
    }

    /**
     * 검증 가능한 JWT의 subject에서 사용자 ID를 추출한다.
     *
     * @param token 사용자 ID를 읽을 JWT
     * @return subject 사용자 ID
     */
    @Override
    public String getUserId(String token) {
        return claims(token).getSubject();
    }

    /**
     * 클라이언트가 액세스 토큰 갱신 시점을 계산할 수 있도록 설정된 유효 시간을 초 단위로 반환한다.
     *
     * @return 액세스 토큰 유효 시간(초)
     */
    @Override
    public long accessTokenExpiresInSeconds() {
        return properties.accessTokenExpiration().toSeconds();
    }

    /**
     * Redis 리프레시 상태의 TTL과 동일하게 사용할 설정 유효 시간을 밀리초 단위로 반환한다.
     *
     * @return 리프레시 토큰 유효 시간(밀리초)
     */
    @Override
    public long refreshTokenExpiresInMillis() {
        return properties.refreshTokenExpiration().toMillis();
    }

    /**
     * 공통 서명키와 필수 발급자를 적용해 JWT payload를 파싱한다.
     *
     * @param token 파싱할 JWT
     * @return 검증된 JWT 클레임
     */
    private Claims claims(String token) {
        return Jwts.parser().verifyWith(key).requireIssuer(properties.issuer()).build()
                .parseSignedClaims(token).getPayload();
    }
}
