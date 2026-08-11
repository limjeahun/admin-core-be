package com.espay.admincore.application.dto.role;

import java.util.List;

/**
 * 페이지 단위로 조회한 관리자 권한 결과.
 *
 * @param items 현재 페이지의 권한 목록
 * @param totalCount 검색 조건에 맞는 전체 권한 수
 * @param page 0부터 시작하는 현재 페이지 번호
 * @param size 페이지당 최대 항목 수
 */
public record RoleListResult(List<RoleResult> items, long totalCount, int page, int size) {
}
