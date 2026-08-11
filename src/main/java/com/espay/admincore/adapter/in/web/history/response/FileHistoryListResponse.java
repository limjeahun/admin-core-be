package com.espay.admincore.adapter.in.web.history.response;

import com.espay.admincore.application.dto.history.FileHistoryListResult;

import java.util.List;

/**
 * 파일 처리 이력 목록의 페이지 HTTP 응답.
 *
 * @param items 현재 페이지의 파일 이력
 * @param totalCount 검색 조건에 맞는 전체 이력 수
 * @param page 현재 페이지 번호
 * @param size 페이지 크기
 */
public record FileHistoryListResponse(List<FileHistoryResponse> items, long totalCount, int page, int size) {
    /**
     * 애플리케이션 결과의 각 항목을 HTTP 응답 모델로 변환한다.
     *
     * @param result 파일 이력 페이지 결과
     * @return 외부 응답용 파일 이력 페이지
     */
    public static FileHistoryListResponse from(FileHistoryListResult result) {
        return new FileHistoryListResponse(result.items().stream().map(FileHistoryResponse::from).toList(),
                result.totalCount(), result.page(), result.size());
    }
}
