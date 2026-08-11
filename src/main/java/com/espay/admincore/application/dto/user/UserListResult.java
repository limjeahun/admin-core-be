package com.espay.admincore.application.dto.user;

import java.util.List;

/**
 * 페이지 단위로 조회한 관리자 사용자 결과.
 *
 * @param items 현재 페이지의 사용자 목록
 * @param totalCount 검색 조건에 맞는 전체 사용자 수
 * @param page 0부터 시작하는 현재 페이지 번호
 * @param size 페이지당 최대 항목 수
 */
public record UserListResult(List<UserResult> items, long totalCount, int page, int size) {
}
