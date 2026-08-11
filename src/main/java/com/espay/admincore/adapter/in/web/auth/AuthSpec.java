package com.espay.admincore.adapter.in.web.auth;

import com.espay.admincore.adapter.in.web.auth.request.LoginRequest;
import com.espay.admincore.adapter.in.web.auth.request.OtpBarcodeIssueRequest;
import com.espay.admincore.adapter.in.web.auth.request.OtpVerifyRequest;
import com.espay.admincore.adapter.in.web.auth.response.CsrfTokenResponse;
import com.espay.admincore.adapter.in.web.auth.response.LoginChallengeResponse;
import com.espay.admincore.adapter.in.web.auth.response.LoginResponse;
import com.espay.admincore.common.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;

/**
 * 로그인, OTP 인증, 토큰 갱신 및 로그아웃 API의 Swagger 명세.
 */
@Tag(name = "로그인/인증 API", description = "로그인, OTP, 토큰 갱신, 로그아웃 API")
public interface AuthSpec {

    /**
     * 사용자 자격 증명을 검증하고 OTP 인증에 필요한 정보를 반환한다.
     *
     * @param request 로그인 ID와 평문 비밀번호를 담은 요청
     * @param clientIp 로그인 이력에 기록할 클라이언트 IP
     * @param userAgent 로그인 이력에 기록할 클라이언트 User-Agent
     * @return OTP 등록 여부와 사전 인증 토큰을 담은 HTTP 응답
     */
    @Operation(summary = "로그인", description = "사용자 ID와 비밀번호를 검증하고 후속 인증에 필요한 정보를 반환합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "로그인 1차 인증 성공",
            content = @Content(schema = @Schema(implementation = LoginChallengeResponse.class))
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "로그인 ID 또는 비밀번호 불일치",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "비활성 사용자 또는 권한",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
    )
    ResponseEntity<ApiResponse<LoginChallengeResponse>> login(
            LoginRequest request,
            @Parameter(hidden = true) String clientIp,
            @Parameter(
                    name = "User-Agent",
                    in = ParameterIn.HEADER,
                    description = "요청 클라이언트 User-Agent",
                    example = "Swagger-UI"
            )
            String userAgent
    );

    /**
     * OTP 등록용 QR 바코드를 발급한다.
     *
     * @param request QR 발급용 사전 인증 토큰과 이메일을 담은 요청
     * @param clientIp OTP 발급 이력에 기록할 클라이언트 IP
     * @param userAgent OTP 발급 이력에 기록할 클라이언트 User-Agent
     * @return QR 생성 및 안내 발송 결과를 나타내는 HTTP 응답
     */
    @Operation(summary = "OTP 바코드 발급", description = "OTP 2차 인증용 QR 코드 정보를 생성하고 발급 절차를 수행합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "OTP 바코드 발급 성공"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "사전 인증 토큰 만료 또는 불일치",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "비활성 사용자 또는 권한",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
    )
    ResponseEntity<ApiResponse<Void>> issueBarcode(
            OtpBarcodeIssueRequest request,
            @Parameter(hidden = true) String clientIp,
            @Parameter(
                    name = "User-Agent",
                    in = ParameterIn.HEADER,
                    description = "요청 클라이언트 User-Agent",
                    example = "Swagger-UI"
            )
            String userAgent
    );

    /**
     * OTP 인증번호를 검증하고 로그인 토큰을 발급한다.
     *
     * @param request 사전 인증 토큰과 사용자가 입력한 OTP 코드를 담은 요청
     * @param clientIp OTP 인증 이력에 기록할 클라이언트 IP
     * @param userAgent OTP 인증 이력에 기록할 클라이언트 User-Agent
     * @return Access Token 응답과 HttpOnly Refresh Token 쿠키
     */
    @Operation(summary = "OTP 인증", description = "입력된 OTP 인증번호를 검증하고 로그인 완료 처리를 수행합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "OTP 인증 성공",
            content = @Content(schema = @Schema(implementation = LoginResponse.class))
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "사전 인증 또는 OTP 검증 실패",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "비활성 사용자 또는 권한",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
    )
    ResponseEntity<ApiResponse<LoginResponse>> verifyOtp(
            OtpVerifyRequest request,
            @Parameter(hidden = true) String clientIp,
            @Parameter(
                    name = "User-Agent",
                    in = ParameterIn.HEADER,
                    description = "요청 클라이언트 User-Agent",
                    example = "Swagger-UI"
            )
            String userAgent
    );

    /**
     * 리프레시 토큰으로 액세스 토큰을 갱신한다.
     *
     * @param refreshToken HttpOnly 쿠키에 저장된 Refresh Token
     * @return 재발급된 Access Token 응답과 회전된 Refresh Token 쿠키
     */
    @Operation(summary = "토큰 갱신", description = "리프레시 토큰을 검증해 액세스 토큰을 재발급합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "토큰 갱신 성공",
            content = @Content(schema = @Schema(implementation = LoginResponse.class))
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Refresh Token 만료·변조 또는 서버 저장값 불일치",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "비활성 사용자 또는 권한",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
    )
    ResponseEntity<ApiResponse<LoginResponse>> refresh(
            @Parameter(hidden = true) String refreshToken);

    /**
     * 현재 사용자의 인증 세션을 종료한다.
     *
     * @param refreshToken 서버 저장 상태와 비교할 Refresh Token 쿠키
     * @return 로그아웃 완료를 나타내는 HTTP 응답
     */
    @Operation(summary = "로그아웃", description = "현재 사용자 세션을 종료하기 위해 인증 토큰 상태를 정리합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "로그아웃 성공"
    )
    ResponseEntity<ApiResponse<Void>> logout(@Parameter(hidden = true) String refreshToken);

    /**
     * 쿠키 인증 요청에 사용할 CSRF 토큰을 발급한다.
     *
     * @param csrfToken Spring Security가 생성한 CSRF 토큰
     * @return CSRF 토큰과 요청 헤더 이름
     */
    @Operation(summary = "CSRF 토큰 발급", description = "Refresh와 로그아웃 요청 헤더에 사용할 CSRF 토큰을 발급합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "CSRF 토큰 발급 성공",
            content = @Content(schema = @Schema(implementation = CsrfTokenResponse.class))
    )
    ResponseEntity<ApiResponse<CsrfTokenResponse>> csrf(
            @Parameter(hidden = true) CsrfToken csrfToken);
}
