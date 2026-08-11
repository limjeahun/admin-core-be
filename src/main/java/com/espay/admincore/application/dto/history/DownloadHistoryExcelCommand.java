package com.espay.admincore.application.dto.history;

/**
 * 이력 검색 결과의 Excel 다운로드를 요청하는 공통 명령.
 *
 * @param <T> 로그인 또는 파일 이력 검색 조건 타입
 * @param query Excel에 포함할 이력 검색 조건
 * @param userId 다운로드를 수행한 감사 대상 사용자 ID
 * @param clientIp 다운로드 요청 클라이언트 IP
 */
public record DownloadHistoryExcelCommand<T>(T query, String userId, String clientIp) {
    /**
     * 검색 조건과 다운로드 감사 정보로 명령을 생성한다.
     * @param query Excel에 포함할 이력 검색 조건
     * @param userId 다운로드 사용자 ID
     * @param clientIp 요청 클라이언트 IP
     * @param <T> 로그인 또는 파일 이력 검색 조건 타입
     * @return 이력 Excel 다운로드 명령
     */
    public static <T> DownloadHistoryExcelCommand<T> of(T query, String userId, String clientIp) {
        return new DownloadHistoryExcelCommand<>(query, userId, clientIp);
    }
}
