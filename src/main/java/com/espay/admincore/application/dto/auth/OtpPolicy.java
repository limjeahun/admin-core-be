package com.espay.admincore.application.dto.auth;

/**
 * OTP 유스케이스가 필요로 하는 프레임워크 독립 설정값.
 * 외부 구성 속성은 애플리케이션 조립 단계에서 이 타입으로 변환한다.
 *
 * @param issuer OTP URI에 기록할 발급자
 * @param qrSize QR 이미지의 가로·세로 픽셀 크기
 * @param maxFailedAttempts 사전 인증 상태를 폐기하기 전 허용할 OTP 실패 횟수
 */
public record OtpPolicy(String issuer, int qrSize, int maxFailedAttempts) {
}
