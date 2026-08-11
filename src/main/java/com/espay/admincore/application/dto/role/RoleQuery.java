package com.espay.admincore.application.dto.role;

/**
 * 관리자 권한 목록을 검색하기 위한 키워드와 페이지 조건.
 *
 * @param keyword 권한명 또는 설명 검색어
 * @param page 0부터 시작하는 페이지 번호
 * @param size 1~100 범위로 보정되는 페이지 크기
 */
public record RoleQuery(String keyword, int page, int size) {
    /**
     * 페이지 번호를 0 이상, 페이지 크기를 1~100 범위로 정규화한다.
     *
     * @param keyword 권한명 또는 설명 검색어
     * @param page 정규화할 페이지 번호
     * @param size 정규화할 페이지 크기
     */
    public RoleQuery {
        page = Math.max(0, page);
        size = Math.min(100, Math.max(1, size));
    }

    /**
     * 검색어와 페이지 정보로 권한 질의를 생성한다.
     * @param keyword 권한명 또는 설명 검색어
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return 정규화된 권한 목록 질의
     */
    public static RoleQuery of(String keyword, int page, int size) {
        return new RoleQuery(keyword, page, size);
    }
}
