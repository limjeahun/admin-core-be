package com.espay.admincore.adapter.in.web.auth.response;

import org.springframework.security.web.csrf.CsrfToken;

/**
 * 쿠키 기반 인증 요청에서 프론트엔드가 전송할 CSRF 헤더 정보.
 *
 * @param token CSRF 요청 헤더에 넣을 토큰 값
 * @param headerName 토큰을 전달할 HTTP 헤더 이름
 */
public record CsrfTokenResponse(String token, String headerName) {

    /**
     * Spring Security가 생성한 CSRF 토큰을 API 응답으로 변환한다.
     *
     * @param csrfToken 현재 요청에 연결된 CSRF 토큰
     * @return 토큰 값과 헤더 이름
     */
    public static CsrfTokenResponse from(CsrfToken csrfToken) {
        return new CsrfTokenResponse(csrfToken.getToken(), csrfToken.getHeaderName());
    }
}
