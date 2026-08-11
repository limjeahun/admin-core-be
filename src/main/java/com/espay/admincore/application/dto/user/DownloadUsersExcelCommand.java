package com.espay.admincore.application.dto.user;

/**
 * 사용자 검색 결과의 Excel 다운로드를 요청하는 명령.
 *
 * @param query Excel에 포함할 사용자 검색 조건
 * @param userId 다운로드를 수행한 감사 대상 사용자 ID
 * @param clientIp 다운로드 요청 클라이언트 IP
 */
public record DownloadUsersExcelCommand(UserQuery query, String userId, String clientIp) {
    /**
     * 사용자 검색 조건과 감사 정보로 다운로드 명령을 생성한다.
     * @param query 사용자 검색 조건
     * @param userId 다운로드 사용자 ID
     * @param clientIp 요청 클라이언트 IP
     * @return 사용자 Excel 다운로드 명령
     */
    public static DownloadUsersExcelCommand of(UserQuery query, String userId, String clientIp) {
        return new DownloadUsersExcelCommand(query, userId, clientIp);
    }
}
