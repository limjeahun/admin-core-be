package com.espay.admincore.application.port.in.auth;

import com.espay.admincore.application.dto.auth.LoginChallengeResult;
import com.espay.admincore.application.dto.auth.LoginCommand;

/**
 * 관리자 아이디와 비밀번호를 이용한 1차 로그인 인증 유스케이스.
 *
 * <p>인증 성공 시 최종 로그인 토큰이 아니라 OTP 인증에 사용할 사전 인증 정보를 반환한다.</p>
 */
public interface LoginUseCase {

    /**
     * 사용자 자격 증명과 접속 정보를 검증하고 OTP 로그인 챌린지를 생성한다.
     *
     * @param command 로그인 ID, 비밀번호, 접속 사유 및 클라이언트 정보
     * @return OTP 단계에서 사용할 사전 인증 토큰과 사용자 요약 정보
     */
    LoginChallengeResult login(LoginCommand command);
}
