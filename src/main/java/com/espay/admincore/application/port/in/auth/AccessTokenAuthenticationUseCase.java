package com.espay.admincore.application.port.in.auth;

import com.espay.admincore.application.dto.auth.AuthenticatedAccount;

/**
 * Bearer Access Token과 현재 사용자·권한 상태를 검증하는 입력 포트.
 */
public interface AccessTokenAuthenticationUseCase {

    /**
     * Access Token을 검증하고 현재도 접근 가능한 인증 계정을 반환한다.
     *
     * @param accessToken Authorization 헤더에서 추출한 Access Token
     * @return 현재 사용자와 권한 상태가 반영된 인증 계정
     */
    AuthenticatedAccount authenticate(String accessToken);
}
