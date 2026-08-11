package com.espay.admincore.adapter.in.web.auth;

import com.espay.admincore.adapter.in.web.auth.cookie.RefreshTokenCookie;
import com.espay.admincore.adapter.in.web.auth.request.*;
import com.espay.admincore.adapter.in.web.auth.response.CsrfTokenResponse;
import com.espay.admincore.adapter.in.web.auth.response.LoginChallengeResponse;
import com.espay.admincore.adapter.in.web.auth.response.LoginResponse;
import com.espay.admincore.application.dto.auth.LoginResult;
import com.espay.admincore.application.port.in.auth.*;
import com.espay.admincore.common.api.ApiResponse;
import com.espay.admincore.adapter.in.web.resolver.ClientIp;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

/**
 * 로그인과 OTP 인증, 토큰 갱신 및 로그아웃 요청을 인증 유스케이스로 전달한다.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController implements AuthSpec {
    private final LoginUseCase              loginUseCase;
    private final OtpBarcodeIssueUseCase    otpBarcodeIssueUseCase;
    private final OtpVerifyUseCase          otpVerifyUseCase;
    private final RefreshTokenUseCase       refreshTokenUseCase;
    private final LogoutUseCase             logoutUseCase;
    private final RefreshTokenCookie        refreshTokenCookie;

    /**
     * 사용자 자격 증명을 검증하고 OTP 인증에 필요한 로그인 챌린지를 반환한다.
     *
     * @param request 로그인 요청 정보
     * @param clientIp 요청 클라이언트 IP
     * @param userAgent 요청 클라이언트 User-Agent
     * @return 로그인 챌린지가 포함된 HTTP 응답
     */
    @PostMapping("/login")
    @Override
    public ResponseEntity<ApiResponse<LoginChallengeResponse>> login(
            @Valid @RequestBody LoginRequest request,
            @Parameter(hidden = true) @ClientIp String clientIp,
            @RequestHeader(value = "User-Agent", defaultValue = "Unknown") String userAgent) {
        return ApiResponse.ok(
                LoginChallengeResponse.from(
                        loginUseCase.login(request.toCommand(clientIp, userAgent))
                )
        ).toResponseEntity();
    }

    /**
     * OTP 등록용 QR 바코드를 생성하고 발급한다.
     *
     * @param request OTP 바코드 발급 요청 정보
     * @param clientIp 요청 클라이언트 IP
     * @param userAgent 요청 클라이언트 User-Agent
     * @return 처리 결과 HTTP 응답
     */
    @PutMapping("/otp/barcode")
    @Override
    public ResponseEntity<ApiResponse<Void>> issueBarcode(
            @Valid @RequestBody OtpBarcodeIssueRequest request,
            @Parameter(hidden = true) @ClientIp String clientIp,
            @RequestHeader(value = "User-Agent", defaultValue = "Unknown") String userAgent) {
        otpBarcodeIssueUseCase.issueBarcode(request.toCommand(clientIp, userAgent));
        return ApiResponse.ok().toResponseEntity();
    }

    /**
     * OTP 인증번호를 검증하고 로그인 토큰을 발급한다.
     *
     * @param request OTP 인증 요청 정보
     * @param clientIp 요청 클라이언트 IP
     * @param userAgent 요청 클라이언트 User-Agent
     * @return 로그인 토큰이 포함된 HTTP 응답
     */
    @PostMapping("/otp/verify")
    @Override
    public ResponseEntity<ApiResponse<LoginResponse>> verifyOtp(
            @Valid @RequestBody OtpVerifyRequest request,
            @Parameter(hidden = true) @ClientIp String clientIp,
            @RequestHeader(value = "User-Agent", defaultValue = "Unknown") String userAgent) {
        LoginResult result = otpVerifyUseCase.verifyOtp(request.toCommand(clientIp, userAgent));
        return ApiResponse.ok(LoginResponse.from(result))
                .toResponseEntity(refreshTokenCookie.issue(result));
    }

    /**
     * 리프레시 토큰을 검증하고 새로운 로그인 토큰을 발급한다.
     *
     * @param refreshToken HttpOnly 쿠키에 저장된 Refresh Token
     * @return 갱신된 로그인 토큰이 포함된 HTTP 응답
     */
    @PostMapping("/refresh")
    @Override
    public ResponseEntity<ApiResponse<LoginResponse>> refresh(
            @CookieValue(name = "${security.refresh-cookie.name}", required = false) String refreshToken) {
        LoginResult result = refreshTokenUseCase.refreshToken(refreshToken);
        return ApiResponse.ok(LoginResponse.from(result))
                .toResponseEntity(refreshTokenCookie.issue(result));
    }

    /**
     * Refresh Token 상태를 제거하고 브라우저 쿠키를 만료해 인증 세션을 종료한다.
     *
     * @param refreshToken HttpOnly 쿠키에 저장된 Refresh Token
     * @return 처리 결과 HTTP 응답
     */
    @PostMapping("/logout")
    @Override
    public ResponseEntity<ApiResponse<Void>> logout(
            @CookieValue(name = "${security.refresh-cookie.name}", required = false) String refreshToken) {
        logoutUseCase.logout(refreshToken);
        return ApiResponse.ok().toResponseEntity(refreshTokenCookie.expire());
    }

    /**
     * Refresh와 로그아웃 요청 헤더에 사용할 CSRF 토큰을 발급한다.
     *
     * @param csrfToken Spring Security가 현재 요청에 생성한 CSRF 토큰
     * @return CSRF 토큰 값과 헤더 이름
     */
    @GetMapping("/csrf")
    @Override
    public ResponseEntity<ApiResponse<CsrfTokenResponse>> csrf(CsrfToken csrfToken) {
        return ApiResponse.ok(CsrfTokenResponse.from(csrfToken)).toResponseEntity();
    }

}
