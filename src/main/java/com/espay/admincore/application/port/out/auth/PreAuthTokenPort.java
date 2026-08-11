package com.espay.admincore.application.port.out.auth;

import com.espay.admincore.application.dto.auth.IssuedPreAuthToken;
import com.espay.admincore.application.dto.auth.PreAuthClaims;
import com.espay.admincore.application.dto.auth.PreAuthPurpose;

/**
 * 비밀번호 인증과 OTP 인증 사이에서 사용하는 단기 사전 인증 토큰 포트.
 */
public interface PreAuthTokenPort {

    /**
     * 지정한 사용자와 인증 목적을 담은 사전 인증 토큰을 발급한다.
     *
     * @param userId 인증 대상 사용자 ID
     * @param purpose 사전 인증 토큰의 사용 목적
     * @return 토큰 문자열, 토큰 식별자 및 만료시간
     */
    IssuedPreAuthToken issue(String userId, PreAuthPurpose purpose);

    /**
     * 사전 인증 토큰의 서명, 발급자, 타입 및 만료시간을 검증하고 클레임을 반환한다.
     *
     * @param token 검증할 사전 인증 토큰
     * @return 토큰 식별자, 사용자 ID 및 인증 목적
     */
    PreAuthClaims verify(String token);
}
