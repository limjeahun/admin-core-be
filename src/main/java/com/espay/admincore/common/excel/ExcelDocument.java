package com.espay.admincore.common.excel;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * 출력 어댑터가 XLSX로 직렬화할 기술 중립적인 Excel 문서.
 *
 * @param sheetName 워크북 시트 이름
 * @param title 문서 제목
 * @param noDataMessage 본문 데이터가 없을 때 표시할 문구
 * @param columns 본문 컬럼 표시 정보
 * @param summaries 문서 상단의 조회 조건 요약
 * @param rows 컬럼 순서에 맞춘 본문 셀 값
 */
public record ExcelDocument(
        String sheetName,
        String title,
        String noDataMessage,
        List<Column> columns,
        List<ExcelSummary> summaries,
        List<List<String>> rows
) {

    private static final int MAX_SHEET_NAME_LENGTH = 31;
    private static final int MAX_COLUMN_WIDTH = 255;
    private static final String INVALID_SHEET_NAME_CHARACTERS = ":\\/?*[]";

    /**
     * 문서 값을 검증하고 모든 목록을 불변 상태로 복사한다.
     *
     * @param sheetName 워크북 시트 이름
     * @param title 문서 제목
     * @param noDataMessage 본문 데이터가 없을 때 표시할 문구
     * @param columns 본문 컬럼 표시 정보
     * @param summaries 문서 상단의 조회 조건 요약
     * @param rows 컬럼 순서에 맞춘 본문 셀 값
     */
    public ExcelDocument {
        sheetName = requireSheetName(sheetName);
        title = requireText(title, "title");
        noDataMessage = requireText(noDataMessage, "noDataMessage");
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

    /**
     * 시트 이름이 Excel 제약을 만족하는지 확인한다.
     *
     * @param value 검증할 시트 이름
     * @return 앞뒤 공백이 제거된 시트 이름
     */
    private static String requireSheetName(String value) {
        String sheetName = requireText(value, "sheetName");
        if (sheetName.length() > MAX_SHEET_NAME_LENGTH) {
            throw new IllegalArgumentException("Excel sheetName must not exceed 31 characters");
        }
        if (sheetName.startsWith("'") || sheetName.endsWith("'") || hasInvalidSheetNameCharacter(sheetName)) {
            throw new IllegalArgumentException("Excel sheetName contains an invalid character");
        }
        return sheetName;
    }

    /**
     * 시트 이름에 Excel에서 허용하지 않는 문자가 있는지 확인한다.
     *
     * @param value 검사할 시트 이름
     * @return 허용하지 않는 문자가 있으면 {@code true}
     */
    private static boolean hasInvalidSheetNameCharacter(String value) {
        return value.chars().anyMatch(character ->
                character == 0
                        || character == 3
                        || INVALID_SHEET_NAME_CHARACTERS.indexOf(character) >= 0
        );
    }

    /**
     * 필수 문자열에 공백이 아닌 값이 있는지 확인한다.
     *
     * @param value 검증할 문자열
     * @param name 예외 메시지에 표시할 값 이름
     * @return 앞뒤 공백이 제거된 문자열
     */
    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Excel " + name + " must not be blank");
        }
        return value.trim();
    }

    /**
     * 한 행의 셀 값을 정규화하고 컬럼 수와 일치하는지 확인한다.
     *
     * @param row 복사할 셀 값 목록
     * @param columnCount 문서 컬럼 수
     * @return null이 빈 문자열로 변환된 불변 셀 값 목록
     */
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
     * 렌더링이 완료된 문서 컬럼의 표시 정보.
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

        /**
         * 컬럼 표시값을 검증하고 실패 키워드를 불변 상태로 복사한다.
         *
         * @param header 헤더 표시명
         * @param width 문자 단위 컬럼 너비
         * @param alignment 본문 셀 정렬
         * @param failureKeywords 실패 결과로 강조할 문자열
         */
        public Column {
            header = requireText(header, "column header");
            if (width <= 0) {
                throw new IllegalArgumentException("Excel column width must be positive");
            }
            if (width > MAX_COLUMN_WIDTH) {
                throw new IllegalArgumentException("Excel column width must not exceed 255");
            }
            alignment = Objects.requireNonNull(alignment, "alignment");
            failureKeywords = Objects.requireNonNull(failureKeywords, "failureKeywords").stream()
                    .map(keyword -> requireText(keyword, "failure keyword"))
                    .toList();
        }

        /**
         * 현재 컬럼이 성공과 실패 결과를 강조하는지 확인한다.
         *
         * @return 실패 키워드가 있으면 {@code true}
         */
        public boolean highlightsResult() {
            return !failureKeywords.isEmpty();
        }

        /**
         * 셀 값이 선언된 실패 키워드를 포함하는지 확인한다.
         *
         * @param value 검사할 셀 문자열
         * @return 실패 키워드를 포함하면 {@code true}
         */
        public boolean isFailure(String value) {
            if (value == null) {
                return false;
            }
            String normalized = value.toLowerCase(Locale.ROOT);
            return failureKeywords.stream()
                    .map(keyword -> keyword.toLowerCase(Locale.ROOT))
                    .anyMatch(normalized::contains);
        }
    }
}
