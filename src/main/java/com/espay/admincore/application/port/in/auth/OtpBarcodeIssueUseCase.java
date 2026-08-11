package com.espay.admincore.application.port.in.auth;

import com.espay.admincore.application.dto.auth.OtpBarcodeIssueCommand;

/**
 * 사용자에게 TOTP 등록용 QR 바코드를 발급하는 유스케이스.
 */
public interface OtpBarcodeIssueUseCase {
    /**
     * 사전 인증 정보를 검증하고 새 OTP 비밀키와 QR 코드를 생성해 이메일로 전송한다.
     *
     * @param command 사전 인증 토큰과 요청 클라이언트 정보
     */
    void issueBarcode(OtpBarcodeIssueCommand command);
}
