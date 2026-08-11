package com.espay.admincore.application.port.in.auth;

/**
 * Refresh Token 쿠키에 연결된 서버 측 로그인 세션을 종료하는 유스케이스.
 */
public interface LogoutUseCase {
    /**
     * 유효한 Refresh Token이 현재 서버 저장값과 일치하면 제거한다.
     * 토큰이 없거나 이미 무효해도 로그아웃을 멱등하게 완료한다.
     *
     * @param refreshToken 클라이언트 쿠키에 담긴 Refresh Token
     */
    void logout(String refreshToken);
}
