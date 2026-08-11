package com.espay.admincore.application.port.in.auth;

import com.espay.admincore.application.dto.auth.LoginResult;
import com.espay.admincore.application.dto.auth.OtpVerifyCommand;

/**
 * 사전 인증 사용자의 TOTP를 검증하고 최종 로그인을 완료하는 유스케이스.
 */
public interface OtpVerifyUseCase {
    /**
     * OTP 번호와 일회성 사전 인증 상태를 검증한 후 Access/Refresh Token을 발급한다.
     *
     * @param command 사전 인증 토큰, 6자리 OTP와 감사 정보
     * @return 최종 로그인 토큰과 사용자·권한 정보
     */
    LoginResult verifyOtp(OtpVerifyCommand command);
}
