package com.espay.admincore.common.excel;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * 출력 어댑터가 XLSX로 직렬화할 기술 중립적인 Excel 문서 모델.
 *
 * @param sheetName 워크북 시트 이름
 * @param title 문서 최상단 제목
 * @param noDataMessage 본문 데이터가 없을 때 표시할 문구
 * @param generationErrorMessage 문서 생성 실패 시 사용할 메시지
 * @param columns 본문 컬럼 메타데이터
 * @param summaries 문서 상단의 조회 조건 요약
 * @param rows 컬럼 순서에 맞춘 본문 셀 값
 */
public record ExcelDocument(
        String sheetName,
        String title,
        String noDataMessage,
        String generationErrorMessage,
        List<Column> columns,
        List<ExcelSummary> summaries,
        List<List<String>> rows
) {

    public ExcelDocument {
        sheetName = requireText(sheetName, "sheetName");
        title = requireText(title, "title");
        noDataMessage = requireText(noDataMessage, "noDataMessage");
        generationErrorMessage = requireText(generationErrorMessage, "generationErrorMessage");
        columns = List.copyOf(Objects.requireNonNull(columns, "columns"));
        summaries = List.copyOf(Objects.requireNonNull(summaries, "summaries"));
        if (columns.isEmpty()) {
            throw new IllegalArgumentException("Excel document must have at least one column");
        }
        int columnCount = columns.size();
        rows = Objects.requireNonNull(rows, "rows").stream()
                .map(row -> copyRow(row, columnCount))
                .toList();
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Excel " + name + " must not be blank");
        }
        return value.trim();
    }

    private static List<String> copyRow(List<String> row, int columnCount) {
        List<String> copied = Objects.requireNonNull(row, "row").stream()
                .map(value -> Objects.requireNonNullElse(value, ""))
                .toList();
        if (copied.size() != columnCount) {
            throw new IllegalArgumentException(
                    "Excel row value count must match column count: expected="
                            + columnCount + ", actual=" + copied.size()
            );
        }
        return copied;
    }

    /**
     * 렌더링이 완료된 문서 컬럼의 표시 메타데이터.
     *
     * @param header 헤더 표시명
     * @param width 문자 단위 컬럼 너비
     * @param alignment 본문 셀 정렬
     * @param failureKeywords 실패 결과로 강조할 문자열
     */
    public record Column(
            String header,
            int width,
            ExcelAlignment alignment,
            List<String> failureKeywords
    ) {

        public Column {
            header = requireText(header, "column header");
            if (width <= 0) {
                throw new IllegalArgumentException("Excel column width must be positive");
            }
            alignment = Objects.requireNonNull(alignment, "alignment");
            failureKeywords = List.copyOf(Objects.requireNonNull(failureKeywords, "failureKeywords"));
        }

        /**
         * 현재 컬럼이 성공·실패 결과 강조 컬럼인지 확인한다.
         */
        public boolean highlightsResult() {
            return !failureKeywords.isEmpty();
        }

        /**
         * 셀 값이 컬럼에 선언된 실패 키워드를 포함하는지 확인한다.
         */
        public boolean isFailure(String value) {
            if (value == null) {
                return false;
            }
            String normalized = value.toLowerCase(Locale.ROOT);
            return failureKeywords.stream()
                    .filter(Objects::nonNull)
                    .map(keyword -> keyword.toLowerCase(Locale.ROOT))
                    .anyMatch(normalized::contains);
        }
    }
}
