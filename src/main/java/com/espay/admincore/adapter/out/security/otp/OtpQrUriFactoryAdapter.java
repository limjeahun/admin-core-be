package com.espay.admincore.adapter.out.security.otp;

import com.espay.admincore.application.dto.auth.CreateOtpQrUriCommand;
import com.espay.admincore.application.port.out.auth.OtpQrUriFactoryPort;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * OTP 앱 호환 TOTP 등록 URI의 레이블과 발급자를 URL 인코딩해 생성하는 어댑터.
 */
@Component
public class OtpQrUriFactoryAdapter implements OtpQrUriFactoryPort {
    /**
     * 6자리·30초 주기의 {@code otpauth://totp} URI를 생성한다.
     *
     * @param command 로그인 ID, 서비스 발급자와 Base32 비밀키를 묶은 생성 명령
     * @return URL 인코딩된 TOTP 등록 URI
     */
    @Override
    public String create(CreateOtpQrUriCommand command) {
        String label = URLEncoder.encode(
                command.issuer() + ":" + command.accountName(), StandardCharsets.UTF_8);
        String encodedIssuer = URLEncoder.encode(command.issuer(), StandardCharsets.UTF_8);
        return "otpauth://totp/%s?secret=%s&issuer=%s&digits=6&period=30"
                .formatted(label, command.secret(), encodedIssuer);
    }
}
