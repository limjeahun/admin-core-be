package com.espay.admincore.adapter.in.web.role.response;

import com.espay.admincore.application.dto.role.RoleListResult;

import java.util.List;

/**
 * 관리자 권한 목록의 페이지 HTTP 응답.
 *
 * @param items 현재 페이지의 권한 목록
 * @param totalCount 전체 권한 수
 * @param page 현재 페이지 번호
 * @param size 페이지 크기
 */
public record RoleListResponse(List<RoleResponse> items, long totalCount, int page, int size) {
    /**
     * 애플리케이션 권한 페이지를 HTTP 응답으로 변환한다.
     *
     * @param result 권한 페이지 결과
     * @return 외부 응답용 권한 페이지
     */
    public static RoleListResponse from(RoleListResult result) {
        return new RoleListResponse(result.items().stream().map(RoleResponse::from).toList(),
                result.totalCount(), result.page(), result.size());
    }
}
