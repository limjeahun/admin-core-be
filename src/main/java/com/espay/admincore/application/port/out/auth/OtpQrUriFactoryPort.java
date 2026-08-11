package com.espay.admincore.application.port.out.auth;

import com.espay.admincore.application.dto.auth.CreateOtpQrUriCommand;

/**
 * OTP 앱이 해석할 수 있는 표준 {@code otpauth://totp} URI를 생성하는 포트.
 */
public interface OtpQrUriFactoryPort {
    /**
     * 계정과 발급자를 식별할 수 있는 TOTP 등록 URI를 생성한다.
     *
     * @param command 계정명, 서비스 발급자와 Base32 비밀키를 묶은 생성 명령
     * @return URL 인코딩된 TOTP 등록 URI
     */
    String create(CreateOtpQrUriCommand command);
}
