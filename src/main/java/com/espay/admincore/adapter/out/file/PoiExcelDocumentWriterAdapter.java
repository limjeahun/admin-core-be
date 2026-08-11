package com.espay.admincore.adapter.out.file;

import com.espay.admincore.application.dto.file.WriteExcelDocumentCommand;
import com.espay.admincore.application.port.out.file.ExcelDocumentWriterPort;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Apache POI를 사용해 표 데이터를 XLSX 문서로 만드는 파일 출력 어댑터.
 */
@Component
public class PoiExcelDocumentWriterAdapter implements ExcelDocumentWriterPort {
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 메모리에서 워크북을 생성하여 제목과 모든 데이터 행을 기록한 뒤 XLSX 바이트로 반환한다.
     *
     * @param command 워크시트명, 열 제목과 데이터 행을 묶은 생성 명령
     * @return 다운로드 응답에 사용할 XLSX 문서 바이트
     * @throws IllegalStateException 워크북 생성 또는 직렬화에 실패한 경우
     */
    @Override
    public byte[] write(WriteExcelDocumentCommand command) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet(command.sheetName());
            CellStyle headerStyle = headerStyle(workbook);
            Row header = sheet.createRow(0);
            for (int index = 0; index < command.headers().size(); index++) {
                Cell cell = header.createCell(index);
                cell.setCellValue(command.headers().get(index));
                cell.setCellStyle(headerStyle);
            }
            for (int rowIndex = 0; rowIndex < command.rows().size(); rowIndex++) {
                Row row = sheet.createRow(rowIndex + 1);
                var values = command.rows().get(rowIndex);
                for (int column = 0; column < values.size(); column++) {
                    writeCell(row.createCell(column), values.get(column));
                }
            }
            for (int index = 0; index < command.headers().size(); index++) {
                sheet.autoSizeColumn(index);
                sheet.setColumnWidth(index, Math.min(sheet.getColumnWidth(index) + 1024, 12000));
            }
            workbook.write(output);
            return output.toByteArray();
        } catch (Exception exception) {
            throw new IllegalStateException("Excel 파일 생성에 실패했습니다.", exception);
        }
    }

    /**
     * 제목 행을 구분하기 위한 굵은 글꼴과 회색 배경의 셀 스타일을 생성한다.
     *
     * @param workbook 스타일과 글꼴을 소유할 워크북
     * @return 제목 셀에 적용할 스타일
     */
    private CellStyle headerStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    /**
     * 값 타입에 따라 숫자·논리값·날짜·문자열 셀로 기록하고 {@code null}은 빈 셀로 처리한다.
     *
     * @param cell 값을 기록할 대상 셀
     * @param value 기록할 원본 값
     */
    private void writeCell(Cell cell, Object value) {
        if (value == null) {
            cell.setBlank();
        } else if (value instanceof Number number) {
            cell.setCellValue(number.doubleValue());
        } else if (value instanceof Boolean bool) {
            cell.setCellValue(bool);
        } else if (value instanceof LocalDateTime dateTime) {
            cell.setCellValue(DATE_TIME.format(dateTime));
        } else if (value instanceof LocalDate date) {
            cell.setCellValue(date.toString());
        } else {
            cell.setCellValue(String.valueOf(value));
        }
    }

}
