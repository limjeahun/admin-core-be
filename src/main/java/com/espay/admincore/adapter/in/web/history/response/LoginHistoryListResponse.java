package com.espay.admincore.adapter.in.web.history.response;

import com.espay.admincore.application.dto.history.LoginHistoryListResult;

import java.util.List;

/**
 * 로그인·OTP 인증 이력 목록의 페이지 HTTP 응답.
 *
 * @param items 현재 페이지의 인증 이력
 * @param totalCount 검색 조건에 맞는 전체 이력 수
 * @param page 현재 페이지 번호
 * @param size 페이지 크기
 */
public record LoginHistoryListResponse(List<LoginHistoryResponse> items, long totalCount, int page, int size) {
    /**
     * 애플리케이션 결과의 각 항목을 HTTP 응답 모델로 변환한다.
     *
     * @param result 로그인 이력 페이지 결과
     * @return 외부 응답용 로그인 이력 페이지
     */
    public static LoginHistoryListResponse from(LoginHistoryListResult result) {
        return new LoginHistoryListResponse(result.items().stream().map(LoginHistoryResponse::from).toList(),
                result.totalCount(), result.page(), result.size());
    }
}
