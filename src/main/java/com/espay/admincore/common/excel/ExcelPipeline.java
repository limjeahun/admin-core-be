package com.espay.admincore.common.excel;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 조회가 끝난 데이터를 Excel 문서로 조립하는 불변 파이프라인.
 *
 * <p>사용 순서는 다음과 같다.</p>
 * <ol>
 *     <li>{@link #from(Iterable)}으로 Excel에 넣을 데이터를 전달한다.</li>
 *     <li>시트 이름, 제목과 안내 문구를 설정한다.</li>
 *     <li>{@link #summary(String, String)}로 문서 상단의 검색 조건을 추가한다.</li>
 *     <li>{@link #column(ExcelColumn)}으로 본문 컬럼을 왼쪽부터 순서대로 정의한다.</li>
 *     <li>{@link #build()}를 호출해 각 행을 셀 값으로 변환하고 {@link ExcelDocument}를 만든다.</li>
 * </ol>
 *
 * <p>데이터 조회는 이 클래스의 책임이 아니다. Application Service가 조회를 완료한 뒤
 * 그 결과를 {@code from()}에 전달해야 한다. 컬럼의 값 추출과 포맷팅은 {@code build()}에서 실행된다.</p>
 *
 * @param <R> Excel 본문 한 행의 원본 타입
 */
public final class ExcelPipeline<R> {

    private final Iterable<? extends R> sourceRows;
    private final String sheetName;
    private final String title;
    private final String noDataMessage;
    private final String generationErrorMessage;
    private final List<ExcelSummary> summaries;
    private final List<ExcelColumn<? super R>> columns;

    /**
     * 현재까지 등록된 데이터와 문서 설정을 보관하는 내부 생성자.
     *
     * <p>목록을 복사해서 보관하므로 이미 만들어진 파이프라인의 설정은 바뀌지 않는다.</p>
     *
     * @param sourceRows 조회가 끝난 Excel 행 원본
     * @param sheetName 워크북 시트 이름
     * @param title 문서 최상단 제목
     * @param noDataMessage 조회 결과가 없을 때 표시할 문구
     * @param generationErrorMessage XLSX 생성 실패 시 사용할 메시지
     * @param summaries 문서 상단에 표시할 요약 목록
     * @param columns 본문 셀 값을 만드는 컬럼 목록
     */
    private ExcelPipeline(
            Iterable<? extends R> sourceRows,
            String sheetName,
            String title,
            String noDataMessage,
            String generationErrorMessage,
            List<ExcelSummary> summaries,
            List<ExcelColumn<? super R>> columns
    ) {
        this.sourceRows = Objects.requireNonNull(sourceRows, "sourceRows");
        this.sheetName = sheetName;
        this.title = title;
        this.noDataMessage = noDataMessage;
        this.generationErrorMessage = generationErrorMessage;
        this.summaries = List.copyOf(summaries);
        this.columns = List.copyOf(columns);
    }

    /**
     * 조회가 끝난 데이터를 전달하고 Excel 파이프라인을 시작한다.
     *
     * <p>예를 들어 {@code from(users)}는 사용자 목록을 문서의 원본 행으로 등록한다.
     * 이 메서드에서는 셀 값을 만들지 않으며 실제 컬럼 변환은 {@link #build()}에서 수행한다.</p>
     *
     * <p>{@link List}뿐 아니라 모든 {@link Iterable} 구현체를 전달할 수 있고,
     * 행 타입의 하위 타입 데이터도 안전하게 받을 수 있다.</p>
     *
     * @param sourceRows 조회가 완료된 Excel 행 원본
     * @param <R> Excel 본문 한 행의 원본 타입
     * @return 행 데이터가 등록된 새 파이프라인
     */
    public static <R> ExcelPipeline<R> from(Iterable<? extends R> sourceRows) {
        return new ExcelPipeline<>(sourceRows, null, null, null, null, List.of(), List.of());
    }

    /**
     * Excel 하단 탭에 표시할 시트 이름을 설정한다.
     *
     * @param value Excel 시트 이름
     * @return 시트 이름이 설정된 새 파이프라인
     */
    public ExcelPipeline<R> sheetName(String value) {
        return copy(value, title, noDataMessage, generationErrorMessage, summaries, columns);
    }

    /**
     * 시트 첫 번째 행에 표시할 문서 제목을 설정한다.
     *
     * @param value Excel 문서 제목
     * @return 제목이 설정된 새 파이프라인
     */
    public ExcelPipeline<R> title(String value) {
        return copy(sheetName, value, noDataMessage, generationErrorMessage, summaries, columns);
    }

    /**
     * 조회 결과가 한 건도 없을 때 본문에 표시할 문구를 설정한다.
     *
     * @param value 빈 결과 안내 문구
     * @return 빈 결과 문구가 설정된 새 파이프라인
     */
    public ExcelPipeline<R> noDataMessage(String value) {
        return copy(sheetName, title, value, generationErrorMessage, summaries, columns);
    }

    /**
     * XLSX 파일 생성에 실패했을 때 사용할 예외 메시지를 설정한다.
     *
     * @param value 문서 생성 실패 메시지
     * @return 생성 실패 메시지가 설정된 새 파이프라인
     */
    public ExcelPipeline<R> generationErrorMessage(String value) {
        return copy(sheetName, title, noDataMessage, value, summaries, columns);
    }

    /**
     * 문서 상단에 라벨과 값으로 표시할 요약 한 줄을 추가한다.
     *
     * <pre>{@code
     * .summary("사용여부", statusCondition(query.status()))
     * }</pre>
     *
     * @param label 문서 상단에 표시할 요약 항목 이름
     * @param value 서비스에서 계산한 요약 표시값
     * @return 요약 항목이 추가된 새 파이프라인
     */
    public ExcelPipeline<R> summary(String label, String value) {
        List<ExcelSummary> appended = new ArrayList<>(summaries);
        appended.add(new ExcelSummary(label, value));
        return copy(sheetName, title, noDataMessage, generationErrorMessage, appended, columns);
    }

    /**
     * 시작일과 종료일을 {@code 시작일 ~ 종료일} 형식의 요약으로 추가한다.
     *
     * <p>시작일이나 종료일이 없는 경우의 표시는
     * {@link ExcelFormatters#dateRange(LocalDate, LocalDate)}가 처리한다.</p>
     *
     * @param label 문서 상단에 표시할 기간 항목 이름
     * @param fromDate 조회 시작일
     * @param toDate 조회 종료일
     * @return 조회 기간 요약이 추가된 새 파이프라인
     */
    public ExcelPipeline<R> summaryDateRange(String label, LocalDate fromDate, LocalDate toDate) {
        return summary(label, ExcelFormatters.dateRange(fromDate, toDate));
    }

    /**
     * 시스템 기본 시간대의 현재 시각을 문서 상단에 추가한다.
     *
     * @param label 문서 상단에 표시할 현재 시각 항목 이름
     * @return 현재 시각 요약이 추가된 새 파이프라인
     */
    public ExcelPipeline<R> summaryNow(String label) {
        return summaryNow(label, Clock.systemDefaultZone());
    }

    /**
     * 전달받은 {@link Clock}을 기준으로 현재 시각을 문서 상단에 추가한다.
     *
     * <p>운영 코드에서는 시스템 Clock을, 테스트에서는 고정 Clock을 사용할 수 있다.</p>
     *
     * @param label 문서 상단에 표시할 현재 시각 항목 이름
     * @param clock 현재 시각과 시간대를 제공할 Clock
     * @return 지정된 Clock의 현재 시각 요약이 추가된 새 파이프라인
     */
    public ExcelPipeline<R> summaryNow(String label, Clock clock) {
        Objects.requireNonNull(clock, "clock");
        return summary(label, ExcelFormatters.dateTime(LocalDateTime.now(clock)));
    }

    /**
     * Excel 본문 컬럼을 한 개 추가한다.
     *
     * <p>호출한 순서가 실제 Excel의 왼쪽부터 오른쪽 컬럼 순서가 된다.</p>
     * <pre>{@code
     * .column(ExcelColumn.text("아이디", 18, AdminUser::getLoginId))
     * .column(ExcelColumn.text("이름", 16, AdminUser::getName))
     * }</pre>
     *
     * <p>현재 행 타입뿐 아니라 그 상위 타입을 처리하는 공통 컬럼도 사용할 수 있다.
     * 셀 값 변환은 {@link #build()}에서 각 행에 적용한다.</p>
     *
     * @param column 행 원본을 하나의 셀 문자열로 바꿀 컬럼 정의
     * @return 본문 컬럼이 추가된 새 파이프라인
     */
    public ExcelPipeline<R> column(ExcelColumn<? super R> column) {
        List<ExcelColumn<? super R>> appended = new ArrayList<>(columns);
        appended.add(Objects.requireNonNull(column, "column"));
        return copy(sheetName, title, noDataMessage, generationErrorMessage, summaries, appended);
    }

    /**
     * 지금까지 등록한 설정과 컬럼을 사용해 완성된 Excel 문서를 만든다.
     *
     * <p>이 메서드는 DB를 조회하지 않는다. {@link #from(Iterable)}에 전달된 각 행을
     * 컬럼 선언 순서대로 문자열 셀 값으로 변환한다.</p>
     *
     * @return 출력 Port에 전달할 완성된 Excel 문서
     * @throws IllegalStateException 필수 문서 설정 또는 컬럼이 누락된 경우
     */
    public ExcelDocument build() {
        requireConfigured(sheetName, "sheetName");
        requireConfigured(title, "title");
        requireConfigured(noDataMessage, "noDataMessage");
        requireConfigured(generationErrorMessage, "generationErrorMessage");
        if (columns.isEmpty()) {
            throw new IllegalStateException("Excel column must be configured");
        }

        List<List<String>> rows = new ArrayList<>();
        for (R row : sourceRows) {
            rows.add(columns.stream()
                    .map(column -> column.render(row))
                    .toList());
        }

        return new ExcelDocument(
                sheetName,
                title,
                noDataMessage,
                generationErrorMessage,
                columns.stream().map(column -> column.definition()).toList(),
                summaries,
                rows
        );
    }

    /**
     * 변경된 설정을 반영한 새 파이프라인을 만든다.
     *
     * <p>기존 파이프라인은 그대로 두고 새 객체를 반환하기 위해 모든 설정 메서드가 사용한다.</p>
     *
     * @param copiedSheetName 새 파이프라인의 시트 이름
     * @param copiedTitle 새 파이프라인의 문서 제목
     * @param copiedNoDataMessage 새 파이프라인의 빈 결과 문구
     * @param copiedGenerationErrorMessage 새 파이프라인의 생성 실패 메시지
     * @param copiedSummaries 새 파이프라인의 요약 목록
     * @param copiedColumns 새 파이프라인의 컬럼 목록
     * @return 변경된 설정을 가진 새 파이프라인
     */
    private ExcelPipeline<R> copy(
            String copiedSheetName,
            String copiedTitle,
            String copiedNoDataMessage,
            String copiedGenerationErrorMessage,
            List<ExcelSummary> copiedSummaries,
            List<ExcelColumn<? super R>> copiedColumns
    ) {
        return new ExcelPipeline<>(
                sourceRows,
                copiedSheetName,
                copiedTitle,
                copiedNoDataMessage,
                copiedGenerationErrorMessage,
                copiedSummaries,
                copiedColumns
        );
    }

    /**
     * 필수 문서 설정이 비어 있지 않은지 확인한다.
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
