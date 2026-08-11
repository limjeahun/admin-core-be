package com.espay.admincore.common.api;

import java.util.List;

/**
 * 페이지 기반 목록 API의 항목과 전체 건수 및 현재 페이지 정보를 전달하는 공통 응답.
 *
 * @param items 현재 페이지의 항목 목록
 * @param totalCount 검색 조건에 맞는 전체 항목 수
 * @param page 0부터 시작하는 현재 페이지 번호
 * @param size 요청한 페이지 크기
 * @param <T> 목록 항목 타입
 */
public record PageResponse<T>(List<T> items, long totalCount, int page, int size) {

    /**
     * 항목 목록이 {@code null}이면 빈 불변 목록으로 바꾸고, 전달된 목록은 방어적으로 복사한다.
     *
     * @param items 현재 페이지의 원본 항목 목록
     * @param totalCount 검색 조건에 맞는 전체 항목 수
     * @param page 0부터 시작하는 현재 페이지 번호
     * @param size 요청한 페이지 크기
     */
    public PageResponse {
        items = items == null ? List.of() : List.copyOf(items);
    }
}
