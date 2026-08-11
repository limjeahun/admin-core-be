package com.espay.admincore.application.dto.auth;

/**
 * 사용자에게 OTP 등록 QR 이미지를 이메일로 발송하도록 요청하는 명령.
 *
 * @param email 수신 이메일 주소
 * @param userName 수신 사용자명
 * @param qrCodeBase64 Base64로 인코딩된 QR 이미지
 */
public record SendOtpBarcodeEmailCommand(String email, String userName, String qrCodeBase64) {
    /**
     * 수신자와 QR 이미지로 메일 발송 명령을 생성한다.
     * @param email 수신 이메일 주소
     * @param userName 수신 사용자명
     * @param qrCodeBase64 Base64 QR 이미지
     * @return OTP 등록 메일 발송 명령
     */
    public static SendOtpBarcodeEmailCommand of(String email, String userName, String qrCodeBase64) {
        return new SendOtpBarcodeEmailCommand(email, userName, qrCodeBase64);
    }
}
