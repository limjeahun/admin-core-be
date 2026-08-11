package com.espay.admincore.application.port.out.auth;

/**
 * 사용자별 TOTP 등록에 사용할 예측 불가능한 비밀키를 생성하는 포트.
 */
public interface OtpSecretGeneratorPort {
    /**
     * 암호학적으로 안전한 난수 기반 Base32 비밀키를 생성한다.
     *
     * @return 패딩이 제거된 Base32 OTP 비밀키
     */
    String generate();
}
