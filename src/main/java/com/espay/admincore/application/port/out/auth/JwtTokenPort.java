package com.espay.admincore.application.port.out.auth;

import com.espay.admincore.application.dto.auth.AccessTokenSubject;

/**
 * 최종 로그인에 사용하는 Access/Refresh JWT의 발급과 검증을 추상화한 포트.
 */
public interface JwtTokenPort {
    /**
     * 인증 사용자와 현재 권한을 담은 Access Token을 발급한다.
     *
     * @param subject 토큰에 기록할 애플리케이션 소유 인증 주체
     * @return 서명된 Access JWT
     */
    String issueAccessToken(AccessTokenSubject subject);
    /**
     * 사용자 ID를 주체로 하는 Refresh Token을 발급한다.
     *
     * @param userId 인증 사용자 ID
     * @return 서명된 Refresh JWT
     */
    String issueRefreshToken(String userId);
    /**
     * 토큰의 서명, 발급자, 만료시간과 타입을 검증한다.
     *
     * @param token 검증할 JWT
     * @param expectedType 기대하는 ACCESS 또는 REFRESH 토큰 종류
     * @return 모든 조건이 유효하면 {@code true}
     */
    boolean isValid(String token, String expectedType);
    /**
     * JWT가 서명은 해석 가능하지만 만료되었는지 확인한다.
     *
     * @param token 확인할 JWT
     * @return 만료된 토큰이면 {@code true}; 다른 형식 오류는 {@code false}
     */
    boolean isExpired(String token);
    /**
     * 검증 가능한 JWT의 subject에서 사용자 ID를 추출한다.
     *
     * @param token 사용자 ID를 읽을 JWT
     * @return 토큰 subject에 저장된 사용자 ID
     */
    String getUserId(String token);
    /**
     * 토큰 응답에 만료 시간을 포함할 수 있도록 Access Token의 유효 시간을 초 단위로 제공한다.
     *
     * @return Access Token 유효 시간(초)
     */
    long accessTokenExpiresInSeconds();

    /**
     * 서버 측 Refresh Token 상태를 같은 기간 보관할 수 있도록 유효 시간을 밀리초 단위로 제공한다.
     *
     * @return Refresh Token과 서버 저장 상태의 유효 시간(밀리초)
     */
    long refreshTokenExpiresInMillis();
}
