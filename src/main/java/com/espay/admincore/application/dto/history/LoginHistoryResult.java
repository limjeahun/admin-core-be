package com.espay.admincore.application.dto.history;

import com.espay.admincore.domain.model.history.LoginHistory;

import java.time.LocalDateTime;

/**
 * 화면과 Excel에 표시할 비밀번호 로그인 또는 OTP 인증 이력.
 *
 * @param historyId 인증 이력 ID
 * @param userId 식별된 사용자 ID
 * @param userName 식별된 사용자명
 * @param loginId 식별된 사용자의 로그인 ID
 * @param authStep LOGIN 또는 OTP 인증 단계
 * @param success 인증 성공 여부
 * @param loginReason 사용자가 입력한 관리자 시스템 접속 사유
 * @param failReason 인증 실패 사유 코드
 * @param inputId 로그인 단계에서 실제 입력된 ID
 * @param clientIp 요청 클라이언트 IP
 * @param userAgent 요청 클라이언트 User-Agent
 * @param createdAt 인증 시도 시각
 */
public record LoginHistoryResult(String historyId, String userId, String userName, String loginId,
                                 String authStep, boolean success, String loginReason, String failReason,
                                 String inputId, String clientIp, String userAgent, LocalDateTime createdAt) {
    /**
     * 로그인 이력 도메인 모델을 애플리케이션 조회 결과로 변환한다.
     *
     * @param history 변환할 로그인 이력 도메인 모델
     * @return 모든 조회 표시 필드를 복사한 결과
     */
    public static LoginHistoryResult from(LoginHistory history) {
        return new LoginHistoryResult(history.getId(), history.getUserId(), history.getUserName(), history.getLoginId(),
                history.getAuthStep(), history.isSuccess(), history.getLoginReason(), history.getFailReason(),
                history.getInputId(), history.getClientIp(), history.getUserAgent(), history.getCreatedAt());
    }
}
