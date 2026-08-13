package com.espay.admincore.common.excel;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * 행에서 셀 값을 추출하고 표시 문자열로 변환하는 불변 컬럼.
 *
 * @param <R> Excel 본문 한 행의 원본 타입
 */
public final class ExcelColumn<R> {

    private static final int MAX_WIDTH = 255;

    private final String header;
    private final int width;
    private final ExcelAlignment alignment;
    private final List<String> failureKeywords;
    private final Function<? super R, ? extends String> renderer;

    /**
     * 표시 정보와 행 변환 함수를 가진 컬럼을 생성한다.
     *
     * @param header 컬럼 헤더
     * @param width 문자 단위 컬럼 너비
     * @param alignment 본문 셀 정렬
     * @param failureKeywords 실패 결과로 강조할 문자열
     * @param renderer 행을 셀 문자열로 변환할 함수
     */
    private ExcelColumn(
            String header,
            int width,
            ExcelAlignment alignment,
            List<String> failureKeywords,
            Function<? super R, ? extends String> renderer
    ) {
        if (header == null || header.isBlank()) {
            throw new IllegalArgumentException("Excel column header must not be blank");
        }
        if (width <= 0) {
            throw new IllegalArgumentException("Excel column width must be positive");
        }
        if (width > MAX_WIDTH) {
            throw new IllegalArgumentException("Excel column width must not exceed 255");
        }
        this.header = header.trim();
        this.width = width;
        this.alignment = Objects.requireNonNull(alignment, "alignment");
        this.failureKeywords = List.copyOf(failureKeywords);
        this.renderer = Objects.requireNonNull(renderer, "renderer");
    }

    /**
     * 행에서 값을 추출하고 지정한 함수로 포맷하는 컬럼을 만든다.
     *
     * @param header 컬럼 헤더
     * @param width 문자 단위 컬럼 너비
     * @param extractor 행에서 원본 값을 추출할 함수
     * @param formatter 원본 값을 문자열로 변환할 함수
     * @param <R> 원본 행 타입
     * @param <V> 추출한 값 타입
     * @return 왼쪽 정렬이 적용된 새 컬럼
     */
    public static <R, V> ExcelColumn<R> formatted(
            String header,
            int width,
            Function<? super R, ? extends V> extractor,
            Function<? super V, ? extends String> formatter
    ) {
        Objects.requireNonNull(extractor, "extractor");
        Objects.requireNonNull(formatter, "formatter");
        return new ExcelColumn<>(
                header,
                width,
                ExcelAlignment.LEFT,
                List.of(),
                row -> Objects.requireNonNullElse(formatter.apply(extractor.apply(row)), "")
        );
    }

    /**
     * 행에서 문자열 값을 추출하는 컬럼을 만든다.
     *
     * @param header 컬럼 헤더
     * @param width 문자 단위 컬럼 너비
     * @param extractor 행에서 문자열을 추출할 함수
     * @param <R> 원본 행 타입
     * @return 문자열 값을 표시하는 새 컬럼
     */
    public static <R> ExcelColumn<R> text(
            String header,
            int width,
            Function<? super R, ? extends String> extractor
    ) {
        return formatted(header, width, extractor, ExcelFormatters::text);
    }

    /**
     * 행에서 날짜 값을 추출하는 컬럼을 만든다.
     *
     * @param header 컬럼 헤더
     * @param width 문자 단위 컬럼 너비
     * @param extractor 행에서 날짜를 추출할 함수
     * @param <R> 원본 행 타입
     * @return 날짜 값을 표시하는 새 컬럼
     */
    public static <R> ExcelColumn<R> date(
            String header,
            int width,
            Function<? super R, ? extends LocalDate> extractor
    ) {
        return formatted(header, width, extractor, ExcelFormatters::date);
    }

    /**
     * 행에서 날짜와 시각 값을 추출하는 컬럼을 만든다.
     *
     * @param header 컬럼 헤더
     * @param width 문자 단위 컬럼 너비
     * @param extractor 행에서 날짜와 시각을 추출할 함수
     * @param <R> 원본 행 타입
     * @return 날짜와 시각 값을 표시하는 새 컬럼
     */
    public static <R> ExcelColumn<R> dateTime(
            String header,
            int width,
            Function<? super R, ? extends LocalDateTime> extractor
    ) {
        return formatted(header, width, extractor, ExcelFormatters::dateTime);
    }

    /**
     * 행에서 KB 단위 파일 크기를 추출하는 컬럼을 만든다.
     *
     * @param header 컬럼 헤더
     * @param width 문자 단위 컬럼 너비
     * @param extractor 행에서 파일 크기를 추출할 함수
     * @param <R> 원본 행 타입
     * @return 파일 크기를 표시하는 새 컬럼
     */
    public static <R> ExcelColumn<R> fileSize(
            String header,
            int width,
            Function<? super R, ? extends Long> extractor
    ) {
        return formatted(header, width, extractor, ExcelFormatters::fileSize);
    }

    /**
     * 가운데 정렬이 적용된 새 컬럼을 반환한다.
     *
     * @return 가운데 정렬이 적용된 새 컬럼
     */
    public ExcelColumn<R> centered() {
        return aligned(ExcelAlignment.CENTER);
    }

    /**
     * 오른쪽 정렬이 적용된 새 컬럼을 반환한다.
     *
     * @return 오른쪽 정렬이 적용된 새 컬럼
     */
    public ExcelColumn<R> rightAligned() {
        return aligned(ExcelAlignment.RIGHT);
    }

    /**
     * 지정한 정렬이 적용된 새 컬럼을 반환한다.
     *
     * @param newAlignment 적용할 본문 셀 정렬
     * @return 지정한 정렬이 적용된 새 컬럼
     * @throws NullPointerException 정렬이 {@code null}인 경우
     */
    public ExcelColumn<R> aligned(ExcelAlignment newAlignment) {
        return new ExcelColumn<>(header, width, newAlignment, failureKeywords, renderer);
    }

    /**
     * 지정한 키워드를 포함한 값을 실패 스타일로 강조한다.
     *
     * @param keywords 실패 결과를 식별할 키워드
     * @return 실패 키워드가 설정된 새 컬럼
     * @throws IllegalArgumentException 키워드가 없거나 공백인 경우
     */
    public ExcelColumn<R> highlightFailures(String... keywords) {
        Objects.requireNonNull(keywords, "keywords");
        List<String> copiedKeywords = Arrays.stream(keywords)
                .map(ExcelColumn::normalizeKeyword)
                .toList();
        if (copiedKeywords.isEmpty()) {
            throw new IllegalArgumentException("Excel result column must have a failure keyword");
        }
        return new ExcelColumn<>(header, width, alignment, copiedKeywords, renderer);
    }

    /**
     * 원본 행을 현재 컬럼의 셀 문자열로 변환한다.
     *
     * @param row 변환할 원본 행
     * @return 변환된 셀 문자열
     */
    String render(R row) {
        return Objects.requireNonNullElse(renderer.apply(row), "");
    }

    /**
     * 렌더링에 필요한 컬럼 표시 정보를 만든다.
     *
     * @return 출력 어댑터에 전달할 컬럼 표시 정보
     */
    ExcelDocument.Column definition() {
        return new ExcelDocument.Column(header, width, alignment, failureKeywords);
    }

    /**
     * 실패 키워드의 앞뒤 공백을 제거하고 유효성을 확인한다.
     *
     * @param keyword 정규화할 실패 키워드
     * @return 앞뒤 공백이 제거된 키워드
     * @throws IllegalArgumentException 키워드가 {@code null}이거나 공백인 경우
     */
    private static String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            throw new IllegalArgumentException("Excel failure keyword must not be blank");
        }
        return keyword.trim();
    }
}
