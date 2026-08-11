package com.espay.admincore.config;

import com.espay.admincore.application.dto.auth.OtpPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 외부 OTP 설정을 애플리케이션에서 사용하는 정책값으로 변환한다.
 */
@Configuration
public class OtpConfig {

    /**
     * OTP 구성 속성에서 프레임워크 독립적인 애플리케이션 정책을 생성한다.
     *
     * @param properties 외부 OTP 구성 속성
     * @return OTP 발급자, QR 크기와 최대 실패 횟수 정책
     */
    @Bean
    public OtpPolicy otpPolicy(OtpProperties properties) {
        return new OtpPolicy(properties.issuer(), properties.qrSize(), properties.maxFailedAttempts());
    }
}
