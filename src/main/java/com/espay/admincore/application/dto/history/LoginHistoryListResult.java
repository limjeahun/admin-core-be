package com.espay.admincore.application.dto.history;

import java.util.List;

/**
 * 페이지 단위로 조회한 로그인·OTP 인증 이력 결과.
 *
 * @param items 현재 페이지의 인증 이력 목록
 * @param totalCount 검색 조건에 맞는 전체 이력 수
 * @param page 0부터 시작하는 현재 페이지 번호
 * @param size 페이지당 최대 항목 수
 */
public record LoginHistoryListResult(List<LoginHistoryResult> items, long totalCount, int page, int size) {
}
