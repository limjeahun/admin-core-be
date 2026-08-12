package com.espay.admincore.common.excel;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * 행 모델에서 한 셀의 값을 추출하고 표시 문자열로 변환하는 타입 안전한 Excel 컬럼 정의.
 *
 * @param <R> Excel 본문 한 행의 원본 타입
 */
public final class ExcelColumn<R> {

    private final String header;
    private final int width;
    private final ExcelAlignment alignment;
    private final List<String> failureKeywords;
    private final Function<? super R, ? extends String> renderer;

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
        this.header = header.trim();
        this.width = width;
        this.alignment = Objects.requireNonNull(alignment, "alignment");
        this.failureKeywords = List.copyOf(failureKeywords);
        this.renderer = Objects.requireNonNull(renderer, "renderer");
    }

    /**
     * 행에서 추출한 중간 값 {@code V}를 formatter로 변환하는 컬럼을 생성한다.
     *
     * <p>extractor는 {@code R}의 상위 타입을 소비하고 {@code V}의 하위 타입을 생산할 수 있으며,
     * formatter는 {@code V}의 상위 타입을 소비할 수 있다.</p>
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

    public static <R> ExcelColumn<R> text(
            String header,
            int width,
            Function<? super R, ? extends String> extractor
    ) {
        return formatted(header, width, extractor, ExcelFormatters::text);
    }

    public static <R> ExcelColumn<R> date(
            String header,
            int width,
            Function<? super R, ? extends LocalDate> extractor
    ) {
        return formatted(header, width, extractor, ExcelFormatters::date);
    }

    public static <R> ExcelColumn<R> dateTime(
            String header,
            int width,
            Function<? super R, ? extends LocalDateTime> extractor
    ) {
        return formatted(header, width, extractor, ExcelFormatters::dateTime);
    }

    public static <R> ExcelColumn<R> fileSize(
            String header,
            int width,
            Function<? super R, ? extends Long> extractor
    ) {
        return formatted(header, width, extractor, ExcelFormatters::fileSize);
    }

    public ExcelColumn<R> centered() {
        return aligned(ExcelAlignment.CENTER);
    }

    public ExcelColumn<R> rightAligned() {
        return aligned(ExcelAlignment.RIGHT);
    }

    public ExcelColumn<R> aligned(ExcelAlignment newAlignment) {
        return new ExcelColumn<>(header, width, newAlignment, failureKeywords, renderer);
    }

    /**
     * 지정된 키워드를 포함하는 값을 실패 결과 스타일로 강조한다.
     */
    public ExcelColumn<R> highlightFailures(String... keywords) {
        Objects.requireNonNull(keywords, "keywords");
        List<String> copiedKeywords = Arrays.stream(keywords)
                .map(keyword -> {
                    if (keyword == null || keyword.isBlank()) {
                        throw new IllegalArgumentException("Excel failure keyword must not be blank");
                    }
                    return keyword.trim();
                })
                .toList();
        if (copiedKeywords.isEmpty()) {
            throw new IllegalArgumentException("Excel result column must have a failure keyword");
        }
        return new ExcelColumn<>(header, width, alignment, copiedKeywords, renderer);
    }

    String render(R row) {
        return Objects.requireNonNullElse(renderer.apply(row), "");
    }

    ExcelDocument.Column definition() {
        return new ExcelDocument.Column(header, width, alignment, failureKeywords);
    }
}
