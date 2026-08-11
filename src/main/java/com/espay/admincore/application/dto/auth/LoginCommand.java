package com.espay.admincore.application.dto.auth;

/**
 * 아이디와 비밀번호를 이용한 1차 로그인을 요청하는 애플리케이션 명령.
 *
 * @param loginId 사용자가 입력한 로그인 ID
 * @param password 사용자가 입력한 원문 비밀번호
 * @param loginReason 관리자 시스템 접속 사유
 * @param clientIp 감사 이력에 기록할 요청 클라이언트 IP
 * @param userAgent 감사 이력에 기록할 요청 User-Agent
 */
public record LoginCommand(String loginId, String password, String loginReason, String clientIp, String userAgent) {
    /**
     * 로그인 자격 증명과 감사 정보로 명령을 생성한다.
     * @param loginId 사용자 로그인 ID
     * @param password 사용자가 입력한 평문 비밀번호
     * @param loginReason 관리자 시스템 접속 사유
     * @param clientIp 요청 클라이언트 IP
     * @param userAgent 요청 User-Agent
     * @return 로그인 명령
     */
    public static LoginCommand of(String loginId, String password, String loginReason,
                                  String clientIp, String userAgent) {
        return new LoginCommand(loginId, password, loginReason, clientIp, userAgent);
    }
}
