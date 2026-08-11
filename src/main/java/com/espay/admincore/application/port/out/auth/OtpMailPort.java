package com.espay.admincore.application.port.out.auth;

import com.espay.admincore.application.dto.auth.SendOtpBarcodeEmailCommand;

/**
 * OTP 등록용 QR 이미지를 사용자 이메일로 전송하는 출력 포트.
 */
public interface OtpMailPort {
    /**
     * Base64 PNG QR 이미지를 본문에 포함한 OTP 안내 메일을 전송한다.
     *
     * @param command 수신자와 Base64 QR 이미지를 묶은 발송 명령
     */
    void sendOtpBarcodeEmail(SendOtpBarcodeEmailCommand command);
}
