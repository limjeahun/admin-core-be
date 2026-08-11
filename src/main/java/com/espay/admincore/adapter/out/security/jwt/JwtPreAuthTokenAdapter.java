package com.espay.admincore.adapter.out.security.jwt;

import com.espay.admincore.application.dto.auth.IssuedPreAuthToken;
import com.espay.admincore.application.dto.auth.PreAuthClaims;
import com.espay.admincore.application.dto.auth.PreAuthPurpose;
import com.espay.admincore.application.port.out.auth.PreAuthTokenPort;
import com.espay.admincore.application.exception.BusinessException;
import com.espay.admincore.application.exception.ErrorCode;
import com.espay.admincore.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

/**
 * HMAC 서명 JWT로 OTP 단계의 단기 사전 인증 토큰을 발급·검증하는 어댑터.
 */
@Component
public class JwtPreAuthTokenAdapter implements PreAuthTokenPort {
    private final JwtProperties properties;
    private final SecretKey key;

    /**
     * 설정된 비밀키 바이트로 HMAC 검증용 키를 초기화한다.
     *
     * @param properties JWT 비밀키, 발급자와 만료시간 설정
     */
    public JwtPreAuthTokenAdapter(JwtProperties properties) {
        this.properties = properties;
        this.key = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 사용자, 목적, 고유 jti와 PRE_AUTH 타입을 담은 단기 JWT를 발급한다.
     *
     * @param userId 인증 대상 사용자 ID
     * @param purpose OTP 로그인 등 사전 인증 토큰의 사용 목적
     * @return 토큰 문자열, jti와 유효시간
     */
    @Override
    public IssuedPreAuthToken issue(String userId, PreAuthPurpose purpose) {
        Instant now = Instant.now();
        String jti = UUID.randomUUID().toString();
        String token = Jwts.builder().issuer(properties.issuer()).subject(userId).id(jti)
                .claim("type", "PRE_AUTH").claim("purpose", purpose.name()).issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(properties.preAuthExpiration()))).signWith(key).compact();
        return new IssuedPreAuthToken(token, jti, properties.preAuthExpiration().toSeconds());
    }

    /**
     * 서명, 발급자, 만료시간과 PRE_AUTH 타입을 검증해 클레임을 반환한다.
     *
     * @param token 검증할 사전 인증 JWT
     * @return 검증된 토큰 ID, 사용자와 목적
     * @throws BusinessException 토큰이 변조·만료되었거나 타입·목적을 해석할 수 없는 경우
     */
    @Override
    public PreAuthClaims verify(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(key).requireIssuer(properties.issuer()).build()
                    .parseSignedClaims(token).getPayload();
            if (!"PRE_AUTH".equals(claims.get("type", String.class))) {
                throw invalid();
            }
            return new PreAuthClaims(
                    claims.getId(),
                    claims.getSubject(),
                    PreAuthPurpose.valueOf(claims.get("purpose", String.class))
            );
        } catch (RuntimeException exception) {
            if (exception instanceof BusinessException businessException) {
                throw businessException;
            }
            throw invalid();
        }
    }

    /**
     * 사전 인증 JWT 검증 오류를 외부에 동일한 401 응답으로 노출하기 위한 예외를 생성한다.
     *
     * @return 사전 인증 정보 오류
     */
    private BusinessException invalid() {
        return new BusinessException(ErrorCode.UNAUTHORIZED, "사전 인증 정보가 유효하지 않거나 만료되었습니다.");
    }
}
