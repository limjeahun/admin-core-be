package com.espay.admincore.adapter.in.web.auth.cookie;

import com.espay.admincore.application.dto.auth.LoginResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 인증 결과의 Refresh Token을 브라우저용 HttpOnly 쿠키로 변환한다.
 */
@Component
@RequiredArgsConstructor
public class RefreshTokenCookie {
    private final RefreshTokenCookieProperties properties;

    /**
     * 발급된 Refresh Token과 만료시간으로 로그인 쿠키를 생성한다.
     *
     * @param result Refresh Token과 만료시간을 포함한 로그인 결과
     * @return 인증 API에만 전송되는 HttpOnly 쿠키
     */
    public ResponseCookie issue(LoginResult result) {
        return ResponseCookie.from(properties.name(), result.refreshToken())
                .httpOnly(true)
                .secure(properties.secure())
                .sameSite(properties.sameSite())
                .path(properties.path())
                .maxAge(Duration.ofMillis(result.refreshTokenExpiresInMillis()))
                .build();
    }

    /**
     * 브라우저의 Refresh Token을 즉시 제거할 만료 쿠키를 생성한다.
     *
     * @return 발급 쿠키와 이름·경로가 같고 Max-Age가 0인 쿠키
     */
    public ResponseCookie expire() {
        return ResponseCookie.from(properties.name(), "")
                .httpOnly(true)
                .secure(properties.secure())
                .sameSite(properties.sameSite())
                .path(properties.path())
                .maxAge(Duration.ZERO)
                .build();
    }
}
