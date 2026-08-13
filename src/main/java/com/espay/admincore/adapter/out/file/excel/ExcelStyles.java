package com.espay.admincore.adapter.out.file.excel;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * 하나의 워크북에서 공통으로 재사용할 셀 스타일 모음.
 *
 * @param title 문서 제목 스타일
 * @param summaryLabel 조회 조건 이름 스타일
 * @param summaryValue 조회 조건 값 스타일
 * @param header 컬럼 헤더 스타일
 * @param bodyLeft 좌측 정렬 본문 스타일
 * @param bodyCenter 가운데 정렬 본문 스타일
 * @param bodyRight 우측 정렬 본문 스타일
 * @param bodyLeftAlternate 교차 행 좌측 정렬 스타일
 * @param bodyCenterAlternate 교차 행 가운데 정렬 스타일
 * @param bodyRightAlternate 교차 행 우측 정렬 스타일
 * @param resultSuccess 성공 결과 스타일
 * @param resultFailure 실패 결과 스타일
 * @param empty 빈 결과 안내 스타일
 */
record ExcelStyles(
        CellStyle title,
        CellStyle summaryLabel,
        CellStyle summaryValue,
        CellStyle header,
        CellStyle bodyLeft,
        CellStyle bodyCenter,
        CellStyle bodyRight,
        CellStyle bodyLeftAlternate,
        CellStyle bodyCenterAlternate,
        CellStyle bodyRightAlternate,
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
    static ExcelStyles create(XSSFWorkbook workbook) {
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
                createBodyStyle(workbook, HorizontalAlignment.RIGHT, IndexedColors.WHITE),
                createBodyStyle(workbook, HorizontalAlignment.LEFT, IndexedColors.GREY_25_PERCENT),
                createBodyStyle(workbook, HorizontalAlignment.CENTER, IndexedColors.GREY_25_PERCENT),
                createBodyStyle(workbook, HorizontalAlignment.RIGHT, IndexedColors.GREY_25_PERCENT),
                createResultStyle(workbook, successFont, IndexedColors.LIGHT_GREEN),
                createResultStyle(workbook, failureFont, IndexedColors.ROSE),
                createStyle(workbook, emptyFont, IndexedColors.WHITE, HorizontalAlignment.CENTER, false)
        );
    }

    /**
     * 글꼴, 배경, 정렬, 줄바꿈과 테두리가 적용된 셀 스타일을 생성한다.
     *
     * @param workbook 스타일을 소유할 워크북
     * @param font 적용할 글꼴
     * @param fillColor 적용할 배경색
     * @param alignment 적용할 가로 정렬
     * @param wrapText 자동 줄바꿈 여부
     * @return 지정한 표시 속성이 적용된 셀 스타일
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
     * @param alignment 적용할 가로 정렬
     * @param fillColor 적용할 배경색
     * @return 본문 셀 스타일
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
     * @param font 적용할 글꼴
     * @param fillColor 적용할 배경색
     * @return 결과 강조 셀 스타일
     */
    private static CellStyle createResultStyle(
            XSSFWorkbook workbook,
            Font font,
            IndexedColors fillColor
    ) {
        return createStyle(workbook, font, fillColor, HorizontalAlignment.CENTER, false);
    }
}
