package com.espay.admincore.common.excel;

import java.util.Objects;

/**
 * Excel 문서 상단에 표시할 조회 조건 요약.
 *
 * @param label 요약 항목 이름
 * @param value 화면에 표시할 값
 */
public record ExcelSummary(String label, String value) {

    /**
     * 요약 이름을 검증하고 null 표시값을 빈 문자열로 변환한다.
     *
     * @param label 요약 항목 이름
     * @param value 화면에 표시할 값
     */
    public ExcelSummary {
        if (label == null || label.isBlank()) {
            throw new IllegalArgumentException("Excel summary label must not be blank");
        }
        label = label.trim();
        value = Objects.requireNonNullElse(value, "");
    }
}
