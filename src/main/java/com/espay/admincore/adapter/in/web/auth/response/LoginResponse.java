package com.espay.admincore.adapter.in.web.auth.response;

import com.espay.admincore.application.dto.auth.LoginResult;

/**
 * OTP 인증 또는 토큰 갱신 후 클라이언트에 반환하는 최종 로그인 정보.
 *
 * @param accessToken 보호된 API에 사용할 Access JWT
 * @param tokenType Authorization 헤더 토큰 유형
 * @param expiresInSeconds Access Token 유효시간(초)
 * @param userId 인증 사용자 ID
 * @param userName 인증 사용자명
 * @param roleId 현재 권한 ID
 * @param roleName 현재 권한명
 */
public record LoginResponse(String accessToken, String tokenType, long expiresInSeconds,
                            String userId, String userName, String roleId, String roleName) {
    /**
     * 애플리케이션 로그인 결과를 외부 HTTP 응답으로 변환한다.
     *
     * @param result 최종 로그인 결과
     * @return API 응답용 토큰과 사용자 정보
     */
    public static LoginResponse from(LoginResult result) {
        return new LoginResponse(result.accessToken(), result.tokenType(),
                result.expiresInSeconds(), result.userId(), result.userName(), result.roleId(),
                result.roleName());
    }
}
