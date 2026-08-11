package com.espay.admincore.adapter.in.web.history.response;

import com.espay.admincore.application.dto.history.LoginHistoryResult;

import java.time.LocalDateTime;

/**
 * 단일 비밀번호 로그인 또는 OTP 인증 감사 이력 HTTP 응답.
 *
 * @param historyId 이력 ID
 * @param userId 식별된 사용자 ID
 * @param userName 사용자명
 * @param loginId 로그인 ID
 * @param authStep LOGIN 또는 OTP 단계
 * @param success 성공 여부
 * @param loginReason 접속 사유
 * @param failReason 실패 사유
 * @param inputId 실제 입력 ID
 * @param clientIp 요청 IP
 * @param userAgent 요청 User-Agent
 * @param createdAt 인증 시도 시각
 */
public record LoginHistoryResponse(String historyId, String userId, String userName, String loginId,
                                   String authStep, boolean success, String loginReason, String failReason,
                                   String inputId, String clientIp, String userAgent, LocalDateTime createdAt) {
    /**
     * 애플리케이션 로그인 이력 결과를 HTTP 응답으로 변환한다.
     *
     * @param result 변환할 로그인 이력 결과
     * @return 외부 응답용 로그인 이력
     */
    public static LoginHistoryResponse from(LoginHistoryResult result) {
        return new LoginHistoryResponse(result.historyId(), result.userId(), result.userName(), result.loginId(),
                result.authStep(), result.success(), result.loginReason(), result.failReason(), result.inputId(),
                result.clientIp(), result.userAgent(), result.createdAt());
    }
}
