package com.espay.admincore.adapter.in.web.auth.request;

import com.espay.admincore.application.dto.auth.OtpVerifyCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 사전 인증 사용자의 TOTP를 검증하는 HTTP 요청.
 *
 * @param preAuthToken 아이디·비밀번호 인증에서 받은 사전 인증 JWT
 * @param otpNumber 숫자로만 구성된 6자리 OTP 번호
 */
public record OtpVerifyRequest(
        @NotBlank String preAuthToken,
        @NotBlank @Pattern(regexp = "^\\d{6}$") String otpNumber
) {
    /**
     * 서버가 수집한 접속 정보를 요청값과 결합해 OTP 검증 명령으로 변환한다.
     *
     * @param clientIp 요청 클라이언트 IP
     * @param userAgent 요청 클라이언트 User-Agent
     * @return OTP 검증 명령
     */
    public OtpVerifyCommand toCommand(String clientIp, String userAgent) {
        return OtpVerifyCommand.of(preAuthToken, otpNumber, clientIp, userAgent);
    }
}
