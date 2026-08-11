package com.espay.admincore.adapter.in.web.user.response;

import com.espay.admincore.application.dto.user.UserListResult;

import java.util.List;

/**
 * 관리자 사용자 목록의 페이지 HTTP 응답.
 *
 * @param items 현재 페이지의 사용자 목록
 * @param totalCount 전체 사용자 수
 * @param page 현재 페이지 번호
 * @param size 페이지 크기
 */
public record UserListResponse(List<UserResponse> items, long totalCount, int page, int size) {
    /**
     * 애플리케이션 사용자 페이지를 HTTP 응답으로 변환한다.
     *
     * @param result 사용자 페이지 결과
     * @return 외부 응답용 사용자 페이지
     */
    public static UserListResponse from(UserListResult result) {
        return new UserListResponse(result.items().stream().map(UserResponse::from).toList(),
                result.totalCount(), result.page(), result.size());
    }
}
