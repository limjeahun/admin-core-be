package com.espay.admincore.application.port.in.auth;

import com.espay.admincore.application.dto.auth.LoginResult;

/**
 * 유효한 Refresh Token을 새로운 토큰 쌍으로 회전하는 유스케이스.
 */
public interface RefreshTokenUseCase {
    /**
     * JWT와 서버 저장 토큰을 모두 검증하고 현재 사용자·권한 기준의 토큰을 재발급한다.
     *
     * @param refreshToken 클라이언트가 제시한 Refresh Token
     * @return 새 Access/Refresh Token과 현재 사용자·권한 정보
     */
    LoginResult refreshToken(String refreshToken);
}
