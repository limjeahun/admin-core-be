package com.espay.admincore.adapter.out.security.otp;

import com.espay.admincore.application.port.out.auth.OtpSecretGeneratorPort;
import org.apache.commons.codec.binary.Base32;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/**
 * {@link SecureRandom}의 160비트 난수를 Base32로 인코딩해 OTP 비밀키를 생성하는 어댑터.
 */
@Component
public class OtpSecretGeneratorAdapter implements OtpSecretGeneratorPort {
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * 20바이트 난수를 생성하고 Base32 패딩 문자를 제거해 반환한다.
     *
     * @return 예측하기 어려운 Base32 OTP 비밀키
     */
    @Override
    public String generate() {
        byte[] bytes = new byte[20];
        secureRandom.nextBytes(bytes);
        return new Base32().encodeToString(bytes).replace("=", "");
    }
}
