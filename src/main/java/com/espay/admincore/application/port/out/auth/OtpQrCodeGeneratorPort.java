package com.espay.admincore.application.port.out.auth;

import com.espay.admincore.application.dto.auth.GenerateOtpQrCodeCommand;

/**
 * OTP 등록 URI를 QR 이미지로 변환하는 출력 포트.
 */
public interface OtpQrCodeGeneratorPort {
    /**
     * 문자열 콘텐츠를 지정 크기의 PNG QR 코드로 생성한다.
     *
     * @param command QR 코드에 포함할 OTP URI와 이미지 크기를 묶은 생성 명령
     * @return Base64로 인코딩된 PNG 이미지
     */
    String generateBase64(GenerateOtpQrCodeCommand command);
}
