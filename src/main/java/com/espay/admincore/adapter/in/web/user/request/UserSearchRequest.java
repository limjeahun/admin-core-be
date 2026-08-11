package com.espay.admincore.adapter.in.web.user.request;

import com.espay.admincore.application.dto.user.UserQuery;

/**
 * 관리자 사용자 목록을 검색하는 HTTP 쿼리 파라미터.
 *
 * @param roleId 권한 ID 필터
 * @param status 사용자 상태 필터
 * @param conditionType 검색어 적용 필드
 * @param keyword 검색어
 * @param page 0부터 시작하는 페이지 번호
 * @param size 페이지 크기
 */
public record UserSearchRequest(String roleId, String status, String conditionType, String keyword,
                                Integer page, Integer size) {
    /**
     * 생략한 페이지 값에 0과 10을 적용해 사용자 검색 조건으로 변환한다.
     *
     * @return 사용자 검색 조건
     */
    public UserQuery toQuery() {
        return UserQuery.of(roleId, status, conditionType, keyword, page == null ? 0 : page, size == null ? 10 : size);
    }
}
