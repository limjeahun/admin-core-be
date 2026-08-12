package com.espay.admincore.common.excel;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExcelPipelineTest {

    @Test
    void 조회가_끝난_데이터를_받고_build에서_컬럼_표시_함수를_실행한다() {
        AtomicInteger renderCount = new AtomicInteger();
        List<Row> rows = List.of(
                new Row("첫 번째", LocalDateTime.of(2026, 8, 11, 9, 30)),
                new Row(null, null)
        );
        Clock clock = Clock.fixed(
                Instant.parse("2026-08-12T01:30:00Z"),
                ZoneId.of("Asia/Seoul")
        );

        ExcelPipeline<Row> pipeline = ExcelPipeline.from(rows)
                .sheetName("test")
                .title("파이프라인 테스트")
                .noDataMessage("데이터가 없습니다.")
                .generationErrorMessage("Excel 생성에 실패했습니다.")
                .summaryDateRange(
                        "조회 기간",
                        LocalDate.of(2026, 8, 1),
                        LocalDate.of(2026, 8, 12)
                )
                .summaryNow("다운로드 일시", clock)
                .column(ExcelColumn.formatted("이름", 20, row -> {
                    renderCount.incrementAndGet();
                    return row.name();
                }, ExcelFormatters::text))
                .column(ExcelColumn.dateTime("생성일시", 22, Row::createdAt).centered());

        assertThat(renderCount).hasValue(0);

        ExcelDocument document = pipeline.build();

        assertThat(renderCount).hasValue(2);
        assertThat(document.summaries())
                .extracting(summary -> summary.label() + "=" + summary.value())
                .containsExactly(
                        "조회 기간=2026-08-01 ~ 2026-08-12",
                        "다운로드 일시=2026-08-12 10:30:00"
                );
        assertThat(document.rows()).containsExactly(
                List.of("첫 번째", "2026-08-11 09:30:00"),
                List.of("", "")
        );
    }

    @Test
    void 메서드_체인은_원본_파이프라인을_변경하지_않는다() {
        ExcelPipeline<Row> base = ExcelPipeline.<Row>from(List.of())
                .sheetName("test")
                .title("불변성 테스트")
                .noDataMessage("데이터가 없습니다.")
                .generationErrorMessage("Excel 생성에 실패했습니다.");

        ExcelPipeline<Row> configured = base.column(ExcelColumn.text("이름", 20, Row::name));

        assertThatThrownBy(base::build)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Excel column must be configured");
        assertThat(configured.build().columns()).hasSize(1);
    }

    @Test
    void 상위_행_타입의_컬럼과_상위_값_타입의_formatter를_조합할_수_있다() {
        Function<Object, String> formatter = Object::toString;
        ExcelColumn<BaseRow> baseColumn = ExcelColumn.formatted(
                "값",
                10,
                BaseRow::value,
                formatter
        );

        ExcelDocument document = ExcelPipeline.<ChildRow>from(List.of(new ChildRow(7)))
                .sheetName("variance")
                .title("Wildcard 테스트")
                .noDataMessage("데이터가 없습니다.")
                .generationErrorMessage("Excel 생성에 실패했습니다.")
                .column(baseColumn)
                .build();

        assertThat(document.rows()).containsExactly(List.of("7"));
    }

    private record Row(String name, LocalDateTime createdAt) {
    }

    private static class BaseRow {
        private final Integer value;

        private BaseRow(Integer value) {
            this.value = value;
        }

        private Integer value() {
            return value;
        }
    }

    private static final class ChildRow extends BaseRow {
        private ChildRow(Integer value) {
            super(value);
        }
    }
}
