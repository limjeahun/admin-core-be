package com.espay.admincore.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * {@code otp} 설정을 OTP 생성·검증 기능에 제공하는 구성 속성.
 *
 * @param serviceName OTP 앱에 표시할 서비스 이름
 * @param issuer OTP URI에 기록할 발급자 이름
 * @param qrSize 생성할 QR 이미지의 가로·세로 픽셀 크기
 * @param maxFailedAttempts 사전 인증 상태를 폐기하기 전 허용할 OTP 실패 횟수
 */
@ConfigurationProperties("otp")
public record OtpProperties(
        String serviceName,
        String issuer,
        int qrSize,
        int maxFailedAttempts
) {
}
