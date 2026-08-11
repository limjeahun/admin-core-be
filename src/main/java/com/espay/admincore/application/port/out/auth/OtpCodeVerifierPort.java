package com.espay.admincore.application.port.out.auth;

/**
 * 등록된 비밀키와 사용자가 입력한 TOTP 번호의 일치 여부를 검증하는 포트.
 */
public interface OtpCodeVerifierPort {
    /**
     * 현재 시각 허용 구간에서 6자리 TOTP 번호를 검증한다.
     *
     * @param encodedSecret Base32로 인코딩된 사용자 OTP 비밀키
     * @param code 사용자가 입력한 6자리 OTP 번호
     * @return 허용 시간 구간의 TOTP와 일치하면 {@code true}
     */
    boolean verify(String encodedSecret, String code);
}
