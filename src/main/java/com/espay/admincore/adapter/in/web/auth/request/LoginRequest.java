package com.espay.admincore.adapter.in.web.auth.request;

import com.espay.admincore.application.dto.auth.LoginCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 아이디·비밀번호 1차 인증을 시작하는 HTTP 요청.
 *
 * @param loginId 4~100자의 관리자 로그인 ID
 * @param password 사용자 원문 비밀번호
 * @param loginReason 최대 100자의 관리자 시스템 접속 사유
 */
public record LoginRequest(
        @NotBlank @Size(min = 4, max = 100) String loginId,
        @NotBlank String password,
        @NotBlank @Size(max = 100) String loginReason
) {
    /**
     * 서버가 수집한 접속 정보를 요청값과 결합해 로그인 명령으로 변환한다.
     *
     * @param clientIp 요청 클라이언트 IP
     * @param userAgent 요청 클라이언트 User-Agent
     * @return 애플리케이션 로그인 명령
     */
    public LoginCommand toCommand(String clientIp, String userAgent) {
        return LoginCommand.of(loginId, password, loginReason, clientIp, userAgent);
    }
}
