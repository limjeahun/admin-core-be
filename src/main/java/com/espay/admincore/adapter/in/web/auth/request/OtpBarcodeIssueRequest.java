package com.espay.admincore.adapter.in.web.auth.request;

import com.espay.admincore.application.dto.auth.OtpBarcodeIssueCommand;
import jakarta.validation.constraints.NotBlank;

/**
 * OTP 등록용 QR 바코드 발급 HTTP 요청.
 *
 * @param preAuthToken 아이디·비밀번호 인증에서 받은 사전 인증 JWT
 */
public record OtpBarcodeIssueRequest(@NotBlank String preAuthToken) {
    /**
     * 서버가 수집한 접속 정보를 요청값과 결합해 OTP 발급 명령으로 변환한다.
     *
     * @param clientIp 요청 클라이언트 IP
     * @param userAgent 요청 클라이언트 User-Agent
     * @return OTP 바코드 발급 명령
     */
    public OtpBarcodeIssueCommand toCommand(String clientIp, String userAgent) {
        return OtpBarcodeIssueCommand.of(preAuthToken, clientIp, userAgent);
    }
}
