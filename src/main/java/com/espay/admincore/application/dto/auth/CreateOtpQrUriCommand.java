package com.espay.admincore.application.dto.auth;

/**
 * OTP 인증 앱에 전달할 표준 등록 URI 생성을 요청하는 명령.
 *
 * @param accountName OTP 앱에 표시할 계정명
 * @param issuer OTP 발급 서비스명
 * @param secret 사용자 OTP 비밀키
 */
public record CreateOtpQrUriCommand(String accountName, String issuer, String secret) {
    /**
     * 계정과 발급자·비밀키로 명령을 생성한다.
     * @param accountName OTP 앱에 표시할 계정명
     * @param issuer OTP 발급 서비스명
     * @param secret 사용자 OTP 비밀키
     * @return OTP 등록 URI 생성 명령
     */
    public static CreateOtpQrUriCommand of(String accountName, String issuer, String secret) {
        return new CreateOtpQrUriCommand(accountName, issuer, secret);
    }
}
