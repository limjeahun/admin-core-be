package com.espay.admincore.application.dto.auth;

/**
 * OTP 등록용 QR 바코드 발급을 요청하는 애플리케이션 명령.
 *
 * @param preAuthToken 비밀번호 인증 단계에서 발급한 사전 인증 토큰
 * @param clientIp 요청 클라이언트 IP
 * @param userAgent 요청 클라이언트 User-Agent
 */
public record OtpBarcodeIssueCommand(String preAuthToken, String clientIp, String userAgent) {
    /**
     * 사전 인증 토큰과 요청 정보로 명령을 생성한다.
     * @param preAuthToken 비밀번호 인증 단계의 사전 인증 토큰
     * @param clientIp 요청 클라이언트 IP
     * @param userAgent 요청 User-Agent
     * @return OTP 바코드 발급 명령
     */
    public static OtpBarcodeIssueCommand of(String preAuthToken, String clientIp, String userAgent) {
        return new OtpBarcodeIssueCommand(preAuthToken, clientIp, userAgent);
    }
}
