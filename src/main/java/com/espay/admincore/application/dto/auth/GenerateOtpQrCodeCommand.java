package com.espay.admincore.application.dto.auth;

/**
 * OTP 등록 URI를 지정한 크기의 QR 이미지로 생성하도록 요청하는 명령.
 *
 * @param content QR 이미지로 인코딩할 OTP 등록 URI
 * @param width 이미지 너비
 * @param height 이미지 높이
 */
public record GenerateOtpQrCodeCommand(String content, int width, int height) {
    /**
     * OTP URI와 이미지 크기로 명령을 생성한다.
     * @param content QR 이미지로 인코딩할 OTP URI
     * @param width 이미지 너비
     * @param height 이미지 높이
     * @return OTP QR 이미지 생성 명령
     */
    public static GenerateOtpQrCodeCommand of(String content, int width, int height) {
        return new GenerateOtpQrCodeCommand(content, width, height);
    }
}
