package com.espay.admincore.application.dto.history;

/**
 * 현재 인증 시도와 가장 가까운 로그인 접속 사유 조회 조건.
 *
 * @param userId 인증 사용자 ID
 * @param inputId 로그인 단계에서 입력한 ID
 * @param clientIp 요청 클라이언트 IP
 */
public record FindLatestLoginReasonQuery(String userId, String inputId, String clientIp) {
    /**
     * 사용자와 입력 ID·IP로 질의를 생성한다.
     * @param userId 인증 사용자 ID
     * @param inputId 로그인 단계에서 입력한 ID
     * @param clientIp 요청 클라이언트 IP
     * @return 최근 로그인 사유 질의
     */
    public static FindLatestLoginReasonQuery of(String userId, String inputId, String clientIp) {
        return new FindLatestLoginReasonQuery(userId, inputId, clientIp);
    }
}
