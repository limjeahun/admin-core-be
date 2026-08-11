package com.espay.admincore.adapter.out.file.excel;

import com.espay.admincore.application.dto.file.ExcelSummaryItem;
import com.espay.admincore.application.dto.file.WriteExcelDocumentCommand;
import com.espay.admincore.application.port.out.file.ExcelDocumentWriterPort;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * 문서 종류에 맞는 관리자 Excel 양식을 Apache POI로 렌더링하는 출력 어댑터.
 */
@Component
public class PoiExcelDocumentWriterAdapter implements ExcelDocumentWriterPort {

    /**
     * 문서 종류에 맞는 템플릿을 선택하고 조회 조건과 본문을 XLSX로 생성한다.
     *
     * @param command 문서 종류, 조회 조건 요약과 본문 행을 묶은 생성 명령
     * @return 다운로드 응답에 사용할 XLSX 문서 바이트
     */
    @Override
    public byte[] write(WriteExcelDocumentCommand command) {
        ExcelSheetTemplate template = resolveTemplate(command);
        try (
                XSSFWorkbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream()
        ) {
            List<String> headers = template.headers();
            Sheet sheet = workbook.createSheet(template.sheetName());
            ExcelStyles styles = ExcelStyles.create(workbook);
            configureSheet(sheet);

            int rowIndex = 0;
            rowIndex = writeTitle(sheet, rowIndex, template.title(), headers.size(), styles);
            rowIndex = writeSummaries(sheet, rowIndex, command.summaries(), headers.size(), styles);
            rowIndex++;

            int headerRowIndex = rowIndex;
            writeHeader(sheet, rowIndex++, headers, styles);

            if (command.rows().isEmpty()) {
                writeEmptyRow(sheet, rowIndex, headers.size(), template.noDataMessage(), styles);
            } else {
                writeRows(sheet, rowIndex, command.rows(), template.stylePolicy(), headers.size(), styles);
            }

            int lastDataRowIndex = command.rows().isEmpty() ? rowIndex : rowIndex + command.rows().size() - 1;
            sheet.createFreezePane(0, headerRowIndex + 1);
            sheet.setAutoFilter(new CellRangeAddress(headerRowIndex, lastDataRowIndex, 0, headers.size() - 1));
            applyColumnWidths(sheet, template.columnWidths());

            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException(template.generationErrorMessage(), exception);
        }
    }

    /**
     * 문서 종류에 대응하는 사용자 또는 이력 템플릿을 선택한다.
     *
     * @param command 문서 종류가 포함된 생성 명령
     * @return 선택된 Excel 시트 템플릿
     */
    private ExcelSheetTemplate resolveTemplate(WriteExcelDocumentCommand command) {
        return switch (command.documentType()) {
            case USERS -> UserExcelTemplate.USERS;
            case LOGIN_HISTORY -> HistoryExcelTemplate.LOGIN;
            case FILE_HISTORY -> HistoryExcelTemplate.FILE;
        };
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
            List<ExcelSummaryItem> summaries,
            int columnCount,
            ExcelStyles styles
    ) {
        for (ExcelSummaryItem summary : summaries) {
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
     * @param stylePolicy 컬럼 정렬과 결과 강조 정책
     * @param columnCount 전체 컬럼 수
     * @param styles 워크북 공통 스타일
     */
    private void writeRows(
            Sheet sheet,
            int startRowIndex,
            List<List<String>> rows,
            ExcelStylePolicy stylePolicy,
            int columnCount,
            ExcelStyles styles
    ) {
        for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
            Row row = sheet.createRow(startRowIndex + rowIndex);
            row.setHeightInPoints(20);
            writeRow(row, rows.get(rowIndex), columnCount, stylePolicy, rowIndex % 2 == 1, styles);
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
     * @param columnCount 전체 컬럼 수
     * @param stylePolicy 컬럼 정렬과 결과 강조 정책
     * @param alternate 교차 행 배경 적용 여부
     * @param styles 워크북 공통 스타일
     */
    private void writeRow(
            Row row,
            List<String> values,
            int columnCount,
            ExcelStylePolicy stylePolicy,
            boolean alternate,
            ExcelStyles styles
    ) {
        for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
            Cell cell = row.createCell(columnIndex);
            String value = columnIndex < values.size() ? values.get(columnIndex) : "";
            cell.setCellValue(safeText(value));
            cell.setCellStyle(resolveBodyStyle(columnIndex, value, stylePolicy, alternate, styles));
        }
    }

    /**
     * 결과 컬럼, 정렬 정책과 교차 행 여부를 기준으로 본문 스타일을 선택한다.
     *
     * @param columnIndex 현재 컬럼 인덱스
     * @param value 현재 셀 값
     * @param stylePolicy 컬럼 정렬과 결과 강조 정책
     * @param alternate 교차 행 배경 적용 여부
     * @param styles 워크북 공통 스타일
     * @return 현재 셀에 적용할 스타일
     */
    private CellStyle resolveBodyStyle(
            int columnIndex,
            String value,
            ExcelStylePolicy stylePolicy,
            boolean alternate,
            ExcelStyles styles
    ) {
        if (stylePolicy.hasResultColumn() && columnIndex == stylePolicy.resultColumnIndex()) {
            return stylePolicy.isFailureValue(value) ? styles.resultFailure() : styles.resultSuccess();
        }
        if (stylePolicy.isCenterAligned(columnIndex)) {
            return alternate ? styles.bodyCenterAlternate() : styles.bodyCenter();
        }
        return alternate ? styles.bodyLeftAlternate() : styles.bodyLeft();
    }

    /**
     * 템플릿에 정의된 문자 단위 너비를 Excel 컬럼 너비로 적용한다.
     *
     * @param sheet 너비를 적용할 시트
     * @param columnWidths 컬럼 순서에 맞는 문자 단위 너비
     */
    private void applyColumnWidths(Sheet sheet, int[] columnWidths) {
        for (int columnIndex = 0; columnIndex < columnWidths.length; columnIndex++) {
            sheet.setColumnWidth(columnIndex, columnWidths[columnIndex] * 256);
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

    /**
     * 하나의 워크북에서 공통으로 재사용할 셀 스타일 모음.
     *
     * @param title 문서 제목 스타일
     * @param summaryLabel 조회 조건 이름 스타일
     * @param summaryValue 조회 조건 값 스타일
     * @param header 컬럼 헤더 스타일
     * @param bodyLeft 좌측 정렬 본문 스타일
     * @param bodyCenter 가운데 정렬 본문 스타일
     * @param bodyLeftAlternate 교차 행 좌측 정렬 스타일
     * @param bodyCenterAlternate 교차 행 가운데 정렬 스타일
     * @param resultSuccess 성공 결과 스타일
     * @param resultFailure 실패 결과 스타일
     * @param empty 빈 결과 안내 스타일
     */
    private record ExcelStyles(
            CellStyle title,
            CellStyle summaryLabel,
            CellStyle summaryValue,
            CellStyle header,
            CellStyle bodyLeft,
            CellStyle bodyCenter,
            CellStyle bodyLeftAlternate,
            CellStyle bodyCenterAlternate,
            CellStyle resultSuccess,
            CellStyle resultFailure,
            CellStyle empty
    ) {

        /**
         * 제목, 요약, 헤더, 본문과 결과 표시에 사용할 스타일을 한 번씩 생성한다.
         *
         * @param workbook 스타일과 글꼴을 소유할 워크북
         * @return 워크북에서 재사용할 스타일 모음
         */
        private static ExcelStyles create(XSSFWorkbook workbook) {
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 15);
            titleFont.setColor(IndexedColors.WHITE.getIndex());

            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());

            Font labelFont = workbook.createFont();
            labelFont.setBold(true);

            Font successFont = workbook.createFont();
            successFont.setBold(true);
            successFont.setColor(IndexedColors.DARK_GREEN.getIndex());

            Font failureFont = workbook.createFont();
            failureFont.setBold(true);
            failureFont.setColor(IndexedColors.DARK_RED.getIndex());

            Font emptyFont = workbook.createFont();
            emptyFont.setItalic(true);
            emptyFont.setColor(IndexedColors.GREY_50_PERCENT.getIndex());

            return new ExcelStyles(
                    createStyle(workbook, titleFont, IndexedColors.DARK_BLUE, HorizontalAlignment.CENTER, true),
                    createStyle(workbook, labelFont, IndexedColors.GREY_25_PERCENT, HorizontalAlignment.CENTER, false),
                    createStyle(workbook, null, IndexedColors.WHITE, HorizontalAlignment.LEFT, false),
                    createStyle(workbook, headerFont, IndexedColors.BLUE_GREY, HorizontalAlignment.CENTER, true),
                    createBodyStyle(workbook, HorizontalAlignment.LEFT, IndexedColors.WHITE),
                    createBodyStyle(workbook, HorizontalAlignment.CENTER, IndexedColors.WHITE),
                    createBodyStyle(workbook, HorizontalAlignment.LEFT, IndexedColors.GREY_25_PERCENT),
                    createBodyStyle(workbook, HorizontalAlignment.CENTER, IndexedColors.GREY_25_PERCENT),
                    createResultStyle(workbook, successFont, IndexedColors.LIGHT_GREEN),
                    createResultStyle(workbook, failureFont, IndexedColors.ROSE),
                    createStyle(workbook, emptyFont, IndexedColors.WHITE, HorizontalAlignment.CENTER, false)
            );
        }

        /**
         * 글꼴, 배경, 정렬, 줄바꿈과 테두리가 적용된 셀 스타일을 생성한다.
         *
         * @param workbook 스타일을 소유할 워크북
         * @param font 적용할 글꼴, 기본 글꼴이면 {@code null}
         * @param fillColor 셀 배경색
         * @param alignment 가로 정렬 방식
         * @param wrapText 자동 줄바꿈 여부
         * @return 생성된 셀 스타일
         */
        private static CellStyle createStyle(
                XSSFWorkbook workbook,
                Font font,
                IndexedColors fillColor,
                HorizontalAlignment alignment,
                boolean wrapText
        ) {
            CellStyle style = workbook.createCellStyle();
            style.setAlignment(alignment);
            style.setVerticalAlignment(VerticalAlignment.CENTER);
            style.setWrapText(wrapText);
            style.setFillForegroundColor(fillColor.getIndex());
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            style.setBorderTop(BorderStyle.THIN);
            style.setBorderRight(BorderStyle.THIN);
            style.setBorderBottom(BorderStyle.THIN);
            style.setBorderLeft(BorderStyle.THIN);
            if (font != null) {
                style.setFont(font);
            }
            return style;
        }

        /**
         * 본문용 정렬, 배경과 자동 줄바꿈이 적용된 스타일을 생성한다.
         *
         * @param workbook 스타일을 소유할 워크북
         * @param alignment 본문 가로 정렬 방식
         * @param fillColor 본문 배경색
         * @return 생성된 본문 셀 스타일
         */
        private static CellStyle createBodyStyle(
                XSSFWorkbook workbook,
                HorizontalAlignment alignment,
                IndexedColors fillColor
        ) {
            return createStyle(workbook, null, fillColor, alignment, true);
        }

        /**
         * 성공 또는 실패 결과를 강조하는 가운데 정렬 스타일을 생성한다.
         *
         * @param workbook 스타일을 소유할 워크북
         * @param font 결과 상태 글꼴
         * @param fillColor 결과 상태 배경색
         * @return 생성된 결과 셀 스타일
         */
        private static CellStyle createResultStyle(
                XSSFWorkbook workbook,
                Font font,
                IndexedColors fillColor
        ) {
            return createStyle(workbook, font, fillColor, HorizontalAlignment.CENTER, false);
        }
    }
}
