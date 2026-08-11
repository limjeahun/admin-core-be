package com.espay.admincore.application.dto.auth;

/**
 * TOTP 번호를 이용한 2차 인증을 요청하는 애플리케이션 명령.
 *
 * @param preAuthToken 비밀번호 인증 단계에서 발급한 사전 인증 토큰
 * @param otpNumber OTP 앱에 표시된 6자리 TOTP 번호
 * @param clientIp 인증 이력에 기록할 요청 클라이언트 IP
 * @param userAgent 인증 이력에 기록할 요청 User-Agent
 */
public record OtpVerifyCommand(String preAuthToken, String otpNumber, String clientIp, String userAgent) {
    /**
     * 사전 인증 토큰과 OTP·요청 정보로 명령을 생성한다.
     * @param preAuthToken 비밀번호 인증 단계의 사전 인증 토큰
     * @param otpNumber 사용자가 입력한 OTP 번호
     * @param clientIp 요청 클라이언트 IP
     * @param userAgent 요청 User-Agent
     * @return OTP 검증 명령
     */
    public static OtpVerifyCommand of(String preAuthToken, String otpNumber, String clientIp, String userAgent) {
        return new OtpVerifyCommand(preAuthToken, otpNumber, clientIp, userAgent);
    }
}
