package com.espay.admincore.application.dto.history;

import java.time.LocalDate;

/**
 * 비밀번호 로그인과 OTP 인증 이력을 검색하기 위한 조건.
 *
 * @param fromDate 검색 시작일(포함)
 * @param toDate 검색 종료일(포함)
 * @param authStep LOGIN 또는 OTP 인증 단계
 * @param success 성공 여부 필터, 전체 조회 시 {@code null}
 * @param conditionType 검색어를 적용할 필드 구분
 * @param keyword 사용자명, 로그인 ID, 입력 ID 등 검색어
 * @param page 0부터 시작하는 페이지 번호
 * @param size 1~100 범위로 보정되는 페이지 크기
 */
public record LoginHistoryQuery(LocalDate fromDate, LocalDate toDate, String authStep, Boolean success,
                                String conditionType, String keyword, int page, int size) {
    /**
     * 페이지 번호를 0 이상, 페이지 크기를 1~100 범위로 정규화한다.
     * 나머지 로그인 이력 검색 조건은 호출자가 전달한 값을 그대로 유지한다.
     *
     * @param fromDate 검색 시작일(포함)
     * @param toDate 검색 종료일(포함)
     * @param authStep 로그인 또는 OTP 인증 단계
     * @param success 성공 여부 필터
     * @param conditionType 검색 필드 구분
     * @param keyword 검색어
     * @param page 정규화할 페이지 번호
     * @param size 정규화할 페이지 크기
     */
    public LoginHistoryQuery {
        page = Math.max(0, page);
        size = Math.min(100, Math.max(1, size));
    }

    /**
     * 기간·인증·검색·페이지 조건으로 로그인 이력 질의를 생성한다.
     * @param fromDate 조회 시작일
     * @param toDate 조회 종료일
     * @param authStep 인증 단계
     * @param success 인증 성공 여부
     * @param conditionType 검색 필드 구분
     * @param keyword 검색어
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return 정규화된 로그인 이력 질의
     */
    public static LoginHistoryQuery of(LocalDate fromDate, LocalDate toDate, String authStep, Boolean success,
                                       String conditionType, String keyword, int page, int size) {
        return new LoginHistoryQuery(fromDate, toDate, authStep, success, conditionType, keyword, page, size);
    }
}
