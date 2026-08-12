package com.espay.admincore.adapter.out.file.excel;

import com.espay.admincore.application.port.out.file.ExcelDocumentWriterPort;
import com.espay.admincore.common.excel.ExcelAlignment;
import com.espay.admincore.common.excel.ExcelDocument;
import com.espay.admincore.common.excel.ExcelSummary;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * 기술 중립적인 Excel 문서 모델을 Apache POI 기반 XLSX로 렌더링하는 출력 어댑터.
 */
@Component
public class PoiExcelDocumentWriterAdapter implements ExcelDocumentWriterPort {

    /**
     * 공통 문서 모델의 제목, 요약, 컬럼과 본문을 XLSX로 생성한다.
     *
     * @param document 출력할 Excel 문서
     * @return 다운로드 응답에 사용할 XLSX 문서 바이트
     */
    @Override
    public byte[] write(ExcelDocument document) {
        try (
                XSSFWorkbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream()
        ) {
            List<String> headers = document.columns().stream()
                    .map(ExcelDocument.Column::header)
                    .toList();
            Sheet sheet = workbook.createSheet(document.sheetName());
            ExcelStyles styles = ExcelStyles.create(workbook);
            configureSheet(sheet);

            int rowIndex = 0;
            rowIndex = writeTitle(sheet, rowIndex, document.title(), headers.size(), styles);
            rowIndex = writeSummaries(sheet, rowIndex, document.summaries(), headers.size(), styles);
            rowIndex++;

            int headerRowIndex = rowIndex;
            writeHeader(sheet, rowIndex++, headers, styles);

            if (document.rows().isEmpty()) {
                writeEmptyRow(sheet, rowIndex, headers.size(), document.noDataMessage(), styles);
            } else {
                writeRows(sheet, rowIndex, document.rows(), document.columns(), styles);
            }

            int lastDataRowIndex = document.rows().isEmpty() ? rowIndex : rowIndex + document.rows().size() - 1;
            sheet.createFreezePane(0, headerRowIndex + 1);
            sheet.setAutoFilter(new CellRangeAddress(headerRowIndex, lastDataRowIndex, 0, headers.size() - 1));
            applyColumnWidths(sheet, document.columns());

            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException(document.generationErrorMessage(), exception);
        }
    }

    /**
     * 눈금선, 확대 비율과 기본 행 높이를 설정한다.
     *
     * @param sheet 표시 옵션을 적용할 시트
     */
    private void configureSheet(Sheet sheet) {
        sheet.setDisplayGridlines(false);
        sheet.setZoom(95);
        sheet.setDefaultRowHeightInPoints(20);
    }

    /**
     * 전체 컬럼을 병합한 문서 제목 행을 작성한다.
     *
     * @param sheet 제목을 작성할 시트
     * @param rowIndex 제목 행 인덱스
     * @param title 문서 제목
     * @param columnCount 전체 컬럼 수
     * @param styles 워크북 공통 스타일
     * @return 제목 다음 행 인덱스
     */
    private int writeTitle(
            Sheet sheet,
            int rowIndex,
            String title,
            int columnCount,
            ExcelStyles styles
    ) {
        Row titleRow = sheet.createRow(rowIndex);
        titleRow.setHeightInPoints(28);
        Cell cell = titleRow.createCell(0);
        cell.setCellValue(safeText(title));
        cell.setCellStyle(styles.title());
        for (int columnIndex = 1; columnIndex < columnCount; columnIndex++) {
            titleRow.createCell(columnIndex).setCellStyle(styles.title());
        }
        sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 0, columnCount - 1));
        return rowIndex + 1;
    }

    /**
     * 조회 조건을 라벨과 값으로 나누고 값 영역을 병합해 작성한다.
     *
     * @param sheet 요약값을 작성할 시트
     * @param rowIndex 요약 시작 행 인덱스
     * @param summaries 조회 조건 요약 목록
     * @param columnCount 전체 컬럼 수
     * @param styles 워크북 공통 스타일
     * @return 마지막 요약 다음 행 인덱스
     */
    private int writeSummaries(
            Sheet sheet,
            int rowIndex,
            List<ExcelSummary> summaries,
            int columnCount,
            ExcelStyles styles
    ) {
        for (ExcelSummary summary : summaries) {
            Row row = sheet.createRow(rowIndex);
            row.setHeightInPoints(20);

            Cell labelCell = row.createCell(0);
            labelCell.setCellValue(safeText(summary.label()));
            labelCell.setCellStyle(styles.summaryLabel());

            Cell valueCell = row.createCell(1);
            valueCell.setCellValue(safeText(summary.value()));
            valueCell.setCellStyle(styles.summaryValue());
            for (int columnIndex = 2; columnIndex < columnCount; columnIndex++) {
                row.createCell(columnIndex).setCellStyle(styles.summaryValue());
            }
            sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 1, columnCount - 1));
            rowIndex++;
        }
        return rowIndex;
    }

    /**
     * 템플릿의 컬럼 헤더를 한 행에 작성한다.
     *
     * @param sheet 헤더를 작성할 시트
     * @param rowIndex 헤더 행 인덱스
     * @param headers 컬럼 헤더 목록
     * @param styles 워크북 공통 스타일
     */
    private void writeHeader(Sheet sheet, int rowIndex, List<String> headers, ExcelStyles styles) {
        Row headerRow = sheet.createRow(rowIndex);
        headerRow.setHeightInPoints(22);
        for (int columnIndex = 0; columnIndex < headers.size(); columnIndex++) {
            Cell cell = headerRow.createCell(columnIndex);
            cell.setCellValue(safeText(headers.get(columnIndex)));
            cell.setCellStyle(styles.header());
        }
    }

    /**
     * 본문 행을 생성하고 행 순서와 컬럼 정책에 맞는 스타일을 적용한다.
     *
     * @param sheet 본문을 작성할 시트
     * @param startRowIndex 본문 시작 행 인덱스
     * @param rows 본문 행 데이터
     * @param columns 컬럼별 정렬과 결과 강조 정책
     * @param styles 워크북 공통 스타일
     */
    private void writeRows(
            Sheet sheet,
            int startRowIndex,
            List<List<String>> rows,
            List<ExcelDocument.Column> columns,
            ExcelStyles styles
    ) {
        for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
            Row row = sheet.createRow(startRowIndex + rowIndex);
            row.setHeightInPoints(20);
            writeRow(row, rows.get(rowIndex), columns, rowIndex % 2 == 1, styles);
        }
    }

    /**
     * 조회 결과가 없을 때 전체 컬럼을 병합한 안내 행을 작성한다.
     *
     * @param sheet 안내 문구를 작성할 시트
     * @param rowIndex 안내 행 인덱스
     * @param columnCount 전체 컬럼 수
     * @param noDataMessage 빈 결과 안내 문구
     * @param styles 워크북 공통 스타일
     */
    private void writeEmptyRow(
            Sheet sheet,
            int rowIndex,
            int columnCount,
            String noDataMessage,
            ExcelStyles styles
    ) {
        Row row = sheet.createRow(rowIndex);
        Cell cell = row.createCell(0);
        cell.setCellValue(safeText(noDataMessage));
        cell.setCellStyle(styles.empty());
        for (int columnIndex = 1; columnIndex < columnCount; columnIndex++) {
            row.createCell(columnIndex).setCellStyle(styles.empty());
        }
        sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 0, columnCount - 1));
    }

    /**
     * 단일 본문 행의 값을 컬럼 순서대로 기록한다.
     *
     * @param row 값을 기록할 행
     * @param values 컬럼 순서에 맞는 셀 값
     * @param columns 컬럼별 표시 정책
     * @param alternate 교차 행 배경 적용 여부
     * @param styles 워크북 공통 스타일
     */
    private void writeRow(
            Row row,
            List<String> values,
            List<ExcelDocument.Column> columns,
            boolean alternate,
            ExcelStyles styles
    ) {
        for (int columnIndex = 0; columnIndex < columns.size(); columnIndex++) {
            Cell cell = row.createCell(columnIndex);
            String value = columnIndex < values.size() ? values.get(columnIndex) : "";
            cell.setCellValue(safeText(value));
            cell.setCellStyle(resolveBodyStyle(columns.get(columnIndex), value, alternate, styles));
        }
    }

    /**
     * 결과 컬럼, 정렬 정책과 교차 행 여부를 기준으로 본문 스타일을 선택한다.
     *
     * @param column 현재 컬럼 정의
     * @param value 현재 셀 값
     * @param alternate 교차 행 배경 적용 여부
     * @param styles 워크북 공통 스타일
     * @return 현재 셀에 적용할 스타일
     */
    private CellStyle resolveBodyStyle(
            ExcelDocument.Column column,
            String value,
            boolean alternate,
            ExcelStyles styles
    ) {
        if (column.highlightsResult()) {
            return column.isFailure(value) ? styles.resultFailure() : styles.resultSuccess();
        }
        if (column.alignment() == ExcelAlignment.CENTER) {
            return alternate ? styles.bodyCenterAlternate() : styles.bodyCenter();
        }
        if (column.alignment() == ExcelAlignment.RIGHT) {
            return alternate ? styles.bodyRightAlternate() : styles.bodyRight();
        }
        return alternate ? styles.bodyLeftAlternate() : styles.bodyLeft();
    }

    /**
     * 문서 컬럼에 정의된 문자 단위 너비를 Excel 컬럼 너비로 적용한다.
     *
     * @param sheet 너비를 적용할 시트
     * @param columns 컬럼 순서에 맞는 표시 정의
     */
    private void applyColumnWidths(Sheet sheet, List<ExcelDocument.Column> columns) {
        for (int columnIndex = 0; columnIndex < columns.size(); columnIndex++) {
            sheet.setColumnWidth(columnIndex, columns.get(columnIndex).width() * 256);
        }
    }

    /**
     * null 셀 값을 빈 문자열로 변환한다.
     *
     * @param value 원본 셀 값
     * @return 원본 문자열 또는 빈 문자열
     */
    private String safeText(String value) {
        return value == null ? "" : value;
    }

}
