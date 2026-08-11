package com.espay.admincore.application.port.out.auth;

import com.espay.admincore.application.dto.auth.SaveRefreshTokenCommand;

/**
 * 사용자별 현재 유효한 Refresh Token을 서버 저장소에서 관리하는 포트.
 */
public interface RefreshTokenPort {
    /**
     * 사용자의 기존 토큰을 교체하고 JWT 만료시간과 동일한 TTL로 저장한다.
     *
     * @param command 사용자, Refresh JWT와 상태 유효시간을 묶은 저장 명령
     */
    void save(SaveRefreshTokenCommand command);

    /**
     * 제시된 기존 토큰이 현재 저장값과 일치할 때만 새로운 토큰으로 원자적으로 교체한다.
     *
     * @param userId 인증 사용자 ID
     * @param currentRefreshToken 클라이언트가 제시한 현재 Refresh Token
     * @param newRefreshToken 교체해 저장할 새로운 Refresh Token
     * @param expirationMillis 새로운 저장 상태의 유효시간(밀리초)
     * @return 기존 토큰이 일치해 교체했으면 {@code true}
     */
    boolean rotate(String userId, String currentRefreshToken, String newRefreshToken, long expirationMillis);

    /**
     * 제시된 토큰이 현재 저장값과 일치할 때만 원자적으로 제거한다.
     *
     * @param userId 로그아웃할 사용자 ID
     * @param refreshToken 로그아웃 요청에 포함된 Refresh Token
     * @return 현재 저장값과 일치해 제거했으면 {@code true}
     */
    boolean deleteIfMatches(String userId, String refreshToken);
}
