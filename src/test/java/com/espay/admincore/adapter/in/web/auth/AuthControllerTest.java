package com.espay.admincore.adapter.in.web.auth;

import com.espay.admincore.adapter.in.web.auth.cookie.RefreshTokenCookie;
import com.espay.admincore.adapter.in.web.auth.cookie.RefreshTokenCookieProperties;
import com.espay.admincore.adapter.in.web.auth.request.OtpVerifyRequest;
import com.espay.admincore.adapter.in.web.auth.response.CsrfTokenResponse;
import com.espay.admincore.adapter.in.web.auth.response.LoginResponse;
import com.espay.admincore.application.dto.auth.LoginResult;
import com.espay.admincore.application.port.in.auth.*;
import com.espay.admincore.common.api.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.DefaultCsrfToken;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {
    @Mock LoginUseCase loginUseCase;
    @Mock OtpBarcodeIssueUseCase otpBarcodeIssueUseCase;
    @Mock OtpVerifyUseCase otpVerifyUseCase;
    @Mock RefreshTokenUseCase refreshTokenUseCase;
    @Mock LogoutUseCase logoutUseCase;

    private AuthController controller;

    @BeforeEach
    void setUp() {
        RefreshTokenCookieProperties properties = new RefreshTokenCookieProperties(
                "admin_refresh_token", false, "Strict", "/api/v1/auth");
        RefreshTokenCookie refreshTokenCookie = new RefreshTokenCookie(properties);
        controller = new AuthController(
                loginUseCase, otpBarcodeIssueUseCase, otpVerifyUseCase,
                refreshTokenUseCase, logoutUseCase, refreshTokenCookie);
    }

    @Test
    void returnsAccessTokenInBodyAndRefreshTokenInHttpOnlyCookieAfterOtp() {
        when(otpVerifyUseCase.verifyOtp(any())).thenReturn(loginResult());

        ResponseEntity<ApiResponse<LoginResponse>> response = controller.verifyOtp(
                new OtpVerifyRequest("pre-auth-token", "123456"), "127.0.0.1", "JUnit");

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().data().accessToken()).isEqualTo("access-token");
        assertThat(response.getBody().data().getClass().getRecordComponents())
                .extracting(component -> component.getName())
                .doesNotContain("refreshToken");
        assertRefreshCookie(response.getHeaders().getFirst(HttpHeaders.SET_COOKIE));
    }

    @Test
    void readsRefreshTokenFromCookieValueAndRotatesCookie() {
        when(refreshTokenUseCase.refreshToken("current-refresh-token")).thenReturn(loginResult());

        ResponseEntity<ApiResponse<LoginResponse>> response =
                controller.refresh("current-refresh-token");

        verify(refreshTokenUseCase).refreshToken("current-refresh-token");
        assertRefreshCookie(response.getHeaders().getFirst(HttpHeaders.SET_COOKIE));
    }

    @Test
    void clearsRefreshTokenCookieOnLogout() {
        ResponseEntity<ApiResponse<Void>> response = controller.logout("refresh-token");

        verify(logoutUseCase).logout("refresh-token");
        String setCookie = response.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        assertThat(setCookie)
                .contains("admin_refresh_token=")
                .contains("Max-Age=0")
                .contains("HttpOnly")
                .contains("SameSite=Strict")
                .contains("Path=/api/v1/auth");
    }

    @Test
    void returnsCsrfTokenAndHeaderName() {
        DefaultCsrfToken token = new DefaultCsrfToken("X-XSRF-TOKEN", "_csrf", "csrf-token");

        ResponseEntity<ApiResponse<CsrfTokenResponse>> response = controller.csrf(token);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().data().token()).isEqualTo("csrf-token");
        assertThat(response.getBody().data().headerName()).isEqualTo("X-XSRF-TOKEN");
    }

    private void assertRefreshCookie(String setCookie) {
        assertThat(setCookie)
                .contains("admin_refresh_token=refresh-token")
                .contains("Max-Age=300")
                .contains("HttpOnly")
                .contains("SameSite=Strict")
                .contains("Path=/api/v1/auth")
                .doesNotContain("Secure");
    }

    private LoginResult loginResult() {
        return new LoginResult(
                "access-token", "refresh-token", "Bearer", 900L, 300_000L,
                "10", "관리자", "1", "운영자");
    }
}
