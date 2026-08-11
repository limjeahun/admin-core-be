package com.espay.admincore.adapter.out.file.excel;

import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Excel 본문 컬럼의 정렬과 처리 결과 강조 규칙.
 *
 * @param centerAlignedColumns 가운데 정렬할 컬럼 인덱스
 * @param resultColumnIndex 성공·실패 강조를 적용할 컬럼 인덱스
 * @param failureKeywords 실패 결과로 판단할 문자열 목록
 */
record ExcelStylePolicy(
        Set<Integer> centerAlignedColumns,
        Integer resultColumnIndex,
        List<String> failureKeywords
) {

    /**
     * 결과 강조 없이 컬럼 정렬만 적용하는 정책을 생성한다.
     *
     * @param centerAlignedColumns 가운데 정렬할 컬럼 인덱스
     * @return 기본 본문 스타일 정책
     */
    static ExcelStylePolicy basic(Set<Integer> centerAlignedColumns) {
        return new ExcelStylePolicy(Set.copyOf(centerAlignedColumns), null, List.of());
    }

    /**
     * 컬럼 정렬과 성공·실패 결과 강조를 함께 적용하는 정책을 생성한다.
     *
     * @param centerAlignedColumns 가운데 정렬할 컬럼 인덱스
     * @param resultColumnIndex 결과를 강조할 컬럼 인덱스
     * @param failureKeywords 실패로 판단할 문자열 목록
     * @return 결과 강조가 포함된 본문 스타일 정책
     */
    static ExcelStylePolicy withResult(
            Set<Integer> centerAlignedColumns,
            int resultColumnIndex,
            List<String> failureKeywords
    ) {
        return new ExcelStylePolicy(Set.copyOf(centerAlignedColumns), resultColumnIndex, List.copyOf(failureKeywords));
    }

    /**
     * 지정한 컬럼을 가운데 정렬해야 하는지 확인한다.
     *
     * @param columnIndex 확인할 컬럼 인덱스
     * @return 가운데 정렬 대상이면 {@code true}
     */
    boolean isCenterAligned(int columnIndex) {
        return centerAlignedColumns.contains(columnIndex);
    }

    /**
     * 처리 결과를 강조할 컬럼이 설정되어 있는지 확인한다.
     *
     * @return 결과 컬럼이 설정되어 있으면 {@code true}
     */
    boolean hasResultColumn() {
        return resultColumnIndex != null;
    }

    /**
     * 셀 값에 실패 키워드가 포함되어 있는지 확인한다.
     *
     * @param value 확인할 처리 결과 값
     * @return 실패 결과이면 {@code true}
     */
    boolean isFailureValue(String value) {
        if (value == null) {
            return false;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        return failureKeywords.stream()
                .map(keyword -> keyword.trim().toLowerCase(Locale.ROOT))
                .anyMatch(normalized::contains);
    }
}
