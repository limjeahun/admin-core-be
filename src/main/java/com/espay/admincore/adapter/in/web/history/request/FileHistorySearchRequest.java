package com.espay.admincore.adapter.in.web.history.request;

import com.espay.admincore.application.dto.history.FileHistoryQuery;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * 파일 업로드·다운로드 이력을 검색하는 HTTP 쿼리 파라미터.
 *
 * @param fromDate 검색 시작일(포함)
 * @param toDate 검색 종료일(포함)
 * @param ioType 업로드 또는 다운로드 구분
 * @param success 성공 여부
 * @param conditionType 검색어 적용 필드
 * @param keyword 검색어
 * @param page 0부터 시작하는 페이지 번호
 * @param size 페이지 크기
 */
public record FileHistorySearchRequest(
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
        String ioType, Boolean success, String conditionType, String keyword, Integer page, Integer size
) {
    /**
     * 생략한 페이지 값에 0과 10을 적용해 애플리케이션 검색 조건으로 변환한다.
     *
     * @return 정규화 가능한 파일 이력 검색 조건
     */
    public FileHistoryQuery toQuery() {
        return FileHistoryQuery.of(fromDate, toDate, ioType, success, conditionType, keyword,
                page == null ? 0 : page, size == null ? 10 : size);
    }
}
