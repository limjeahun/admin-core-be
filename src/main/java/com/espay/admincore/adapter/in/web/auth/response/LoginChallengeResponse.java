package com.espay.admincore.adapter.in.web.auth.response;

import com.espay.admincore.application.dto.auth.LoginChallengeResult;

/**
 * 아이디·비밀번호 1차 인증 성공 후 클라이언트에 반환하는 OTP 로그인 챌린지.
 *
 * @param preAuthToken OTP 등록 또는 검증에 사용할 사전 인증 토큰
 * @param expiresInSeconds 사전 인증 유효시간(초)
 * @param loginId 인증된 로그인 ID
 * @param name 인증된 사용자명
 * @param otpRegistered OTP 비밀키 등록 여부
 */
public record LoginChallengeResponse(String preAuthToken, long expiresInSeconds, String loginId,
                                     String name, boolean otpRegistered) {
    /**
     * 애플리케이션 로그인 챌린지를 외부 HTTP 응답으로 변환한다.
     *
     * @param result 1차 인증 결과
     * @return API 응답용 로그인 챌린지
     */
    public static LoginChallengeResponse from(LoginChallengeResult result) {
        return new LoginChallengeResponse(result.preAuthToken(), result.expiresInSeconds(), result.loginId(),
                result.name(), result.otpRegistered());
    }
}
