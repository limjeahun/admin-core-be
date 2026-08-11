package com.espay.admincore.application.dto.file;

/**
 * Excel 상단 조회 조건 영역에 표시할 라벨과 값.
 *
 * @param label 조회 조건 이름
 * @param value 조회 조건 표시값
 */
public record ExcelSummaryItem(String label, String value) {

    /**
     * 조회 조건 이름과 표시값을 묶어 요약 항목을 생성한다.
     *
     * @param label 조회 조건 이름
     * @param value 조회 조건 표시값
     * @return Excel 요약 항목
     */
    public static ExcelSummaryItem of(String label, String value) {
        return new ExcelSummaryItem(label, value);
    }
}
