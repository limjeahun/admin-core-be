package com.espay.admincore.common.excel;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * 조회된 행을 기술 중립적인 Excel 문서로 변환하는 불변 파이프라인.
 *
 * @param <R> Excel 본문 한 행의 원본 타입
 */
public final class ExcelPipeline<R> {

    private final List<R> sourceRows;
    private final String sheetName;
    private final String title;
    private final String noDataMessage;
    private final List<ExcelSummary> summaries;
    private final List<ExcelColumn<? super R>> columns;

    /**
     * 원본 행과 현재 문서 설정을 보관하는 파이프라인을 생성한다.
     *
     * @param sourceRows 변경할 수 없는 원본 행 목록
     * @param sheetName 워크북 시트 이름
     * @param title 문서 제목
     * @param noDataMessage 조회 결과가 없을 때 표시할 문구
     * @param summaries 문서 상단의 요약 목록
     * @param columns 본문 컬럼 목록
     */
    private ExcelPipeline(
            List<R> sourceRows,
            String sheetName,
            String title,
            String noDataMessage,
            List<ExcelSummary> summaries,
            List<ExcelColumn<? super R>> columns
    ) {
        this.sourceRows = Objects.requireNonNull(sourceRows, "sourceRows");
        this.sheetName = sheetName;
        this.title = title;
        this.noDataMessage = noDataMessage;
        this.summaries = List.copyOf(summaries);
        this.columns = List.copyOf(columns);
    }

    /**
     * 원본 행을 복사해 변경되지 않는 파이프라인을 시작한다.
     *
     * @param sourceRows 조회가 완료된 원본 행
     * @param <R> Excel 본문 한 행의 원본 타입
     * @return 원본 행의 불변 스냅샷을 가진 새 파이프라인
     * @throws NullPointerException 원본 또는 원본 행이 {@code null}인 경우
     */
    public static <R> ExcelPipeline<R> from(Collection<? extends R> sourceRows) {
        return new ExcelPipeline<>(List.copyOf(sourceRows), null, null, null, List.of(), List.of());
    }

    /**
     * 시트 이름이 설정된 새 파이프라인을 반환한다.
     *
     * @param value Excel 시트 이름
     * @return 시트 이름이 설정된 새 파이프라인
     */
    public ExcelPipeline<R> sheetName(String value) {
        return copy(value, title, noDataMessage, summaries, columns);
    }

    /**
     * 문서 제목이 설정된 새 파이프라인을 반환한다.
     *
     * @param value Excel 문서 제목
     * @return 문서 제목이 설정된 새 파이프라인
     */
    public ExcelPipeline<R> title(String value) {
        return copy(sheetName, value, noDataMessage, summaries, columns);
    }

    /**
     * 빈 결과 안내 문구가 설정된 새 파이프라인을 반환한다.
     *
     * @param value 빈 결과 안내 문구
     * @return 빈 결과 안내 문구가 설정된 새 파이프라인
     */
    public ExcelPipeline<R> noDataMessage(String value) {
        return copy(sheetName, title, value, summaries, columns);
    }

    /**
     * 문서 상단에 라벨과 값으로 구성된 요약을 추가한다.
     *
     * @param label 요약 항목 이름
     * @param value 요약 표시값
     * @return 요약이 추가된 새 파이프라인
     */
    public ExcelPipeline<R> summary(String label, String value) {
        List<ExcelSummary> appended = new ArrayList<>(summaries);
        appended.add(new ExcelSummary(label, value));
        return copy(sheetName, title, noDataMessage, appended, columns);
    }

    /**
     * 시작일과 종료일을 조회 기간 요약으로 추가한다.
     *
     * @param label 조회 기간 항목 이름
     * @param fromDate 조회 시작일
     * @param toDate 조회 종료일
     * @return 조회 기간 요약이 추가된 새 파이프라인
     */
    public ExcelPipeline<R> summaryDateRange(String label, LocalDate fromDate, LocalDate toDate) {
        return summary(label, ExcelFormatters.dateRange(fromDate, toDate));
    }

    /**
     * 시스템 기본 시간대의 현재 시각을 요약으로 추가한다.
     *
     * @param label 현재 시각 항목 이름
     * @return 현재 시각 요약이 추가된 새 파이프라인
     */
    public ExcelPipeline<R> summaryNow(String label) {
        return summaryNow(label, Clock.systemDefaultZone());
    }

    /**
     * 전달받은 시계를 기준으로 현재 시각을 요약으로 추가한다.
     *
     * @param label 현재 시각 항목 이름
     * @param clock 현재 시각과 시간대를 제공할 시계
     * @return 현재 시각 요약이 추가된 새 파이프라인
     * @throws NullPointerException 시계가 {@code null}인 경우
     */
    public ExcelPipeline<R> summaryNow(String label, Clock clock) {
        Objects.requireNonNull(clock, "clock");
        return summary(label, ExcelFormatters.dateTime(LocalDateTime.now(clock)));
    }

    /**
     * 선언 순서에 맞춰 본문 컬럼을 추가한다.
     *
     * @param column 행을 셀 문자열로 변환할 컬럼
     * @return 컬럼이 추가된 새 파이프라인
     * @throws NullPointerException 컬럼이 {@code null}인 경우
     */
    public ExcelPipeline<R> column(ExcelColumn<? super R> column) {
        List<ExcelColumn<? super R>> appended = new ArrayList<>(columns);
        appended.add(Objects.requireNonNull(column, "column"));
        return copy(sheetName, title, noDataMessage, summaries, appended);
    }

    /**
     * 원본 행을 컬럼 순서대로 변환해 완성된 문서를 만든다.
     *
     * @return 출력 포트에 전달할 Excel 문서
     * @throws IllegalStateException 필수 설정 또는 컬럼이 누락된 경우
     */
    public ExcelDocument build() {
        validate();
        return new ExcelDocument(
                sheetName,
                title,
                noDataMessage,
                columns.stream()
                        .map(ExcelColumn::definition)
                        .toList(),
                summaries,
                renderRows()
        );
    }

    /**
     * 모든 원본 행을 Excel 셀 값 목록으로 변환한다.
     *
     * @return 원본 행 순서가 유지된 셀 값 목록
     */
    private List<List<String>> renderRows() {
        return sourceRows.stream()
                .map(this::renderRow)
                .toList();
    }

    /**
     * 한 행을 선언된 컬럼 순서대로 변환한다.
     *
     * @param row 변환할 원본 행
     * @return 컬럼 순서에 맞춘 셀 문자열 목록
     */
    private List<String> renderRow(R row) {
        return columns.stream()
                .map(column -> column.render(row))
                .toList();
    }

    /**
     * 변경된 설정을 반영한 새 파이프라인을 생성한다.
     *
     * @param copiedSheetName 새 시트 이름
     * @param copiedTitle 새 문서 제목
     * @param copiedNoDataMessage 새 빈 결과 문구
     * @param copiedSummaries 새 요약 목록
     * @param copiedColumns 새 컬럼 목록
     * @return 변경된 설정을 가진 새 파이프라인
     */
    private ExcelPipeline<R> copy(
            String copiedSheetName,
            String copiedTitle,
            String copiedNoDataMessage,
            List<ExcelSummary> copiedSummaries,
            List<ExcelColumn<? super R>> copiedColumns
    ) {
        return new ExcelPipeline<>(
                sourceRows,
                copiedSheetName,
                copiedTitle,
                copiedNoDataMessage,
                copiedSummaries,
                copiedColumns
        );
    }

    /**
     * 문서 생성에 필요한 설정이 모두 등록됐는지 확인한다.
     *
     * @throws IllegalStateException 필수 설정 또는 컬럼이 누락된 경우
     */
    private void validate() {
        requireConfigured(sheetName, "sheetName");
        requireConfigured(title, "title");
        requireConfigured(noDataMessage, "noDataMessage");
        if (columns.isEmpty()) {
            throw new IllegalStateException("Excel column must be configured");
        }
    }

    /**
     * 필수 문자열 설정에 공백이 아닌 값이 있는지 확인한다.
     *
     * @param value 검증할 설정값
     * @param name 예외 메시지에 표시할 설정 이름
     * @throws IllegalStateException 값이 {@code null}이거나 공백인 경우
     */
    private static void requireConfigured(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Excel " + name + " must be configured");
        }
    }
}
