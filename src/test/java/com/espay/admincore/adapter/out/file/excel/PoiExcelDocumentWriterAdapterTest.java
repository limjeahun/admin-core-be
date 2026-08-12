package com.espay.admincore.adapter.out.file.excel;

import com.espay.admincore.common.excel.ExcelAlignment;
import com.espay.admincore.common.excel.ExcelDocument;
import com.espay.admincore.common.excel.ExcelSummary;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PoiExcelDocumentWriterAdapterTest {
    private final PoiExcelDocumentWriterAdapter writer = new PoiExcelDocumentWriterAdapter();

    @Test
    void 사용자_문서에_제목_요약_헤더와_공통_양식을_적용한다() throws IOException {
        byte[] bytes = writer.write(new ExcelDocument(
                "users",
                "사용자 관리",
                "조회된 사용자가 없습니다.",
                "사용자 Excel 파일 생성에 실패했습니다.",
                List.of(
                        column("아이디", 18, ExcelAlignment.LEFT),
                        column("이름", 16, ExcelAlignment.LEFT),
                        column("이메일", 30, ExcelAlignment.LEFT),
                        column("휴대폰번호", 18, ExcelAlignment.CENTER),
                        column("부서", 18, ExcelAlignment.LEFT),
                        column("권한", 18, ExcelAlignment.LEFT),
                        column("상태", 14, ExcelAlignment.CENTER),
                        column("최종 로그인", 22, ExcelAlignment.CENTER)
                ),
                List.of(
                        new ExcelSummary("권한그룹", "전체"),
                        new ExcelSummary("사용여부", "이용중"),
                        new ExcelSummary("조회조건", "이름 / 관리자"),
                        new ExcelSummary("다운로드 일시", "2026-08-11 16:00:00")
                ),
                List.of(
                        List.of("master", "초기 관리자", "admin@example.com", "010-0000-0000", "운영", "MASTER", "이용중", "2026-08-11 15:00:00"),
                        List.of("operator", "운영자", "operator@example.com", "", "운영", "OPERATOR", "이용중지", "")
                )
        ));

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            XSSFSheet sheet = workbook.getSheetAt(0);

            assertThat(sheet.getSheetName()).isEqualTo("users");
            assertThat(sheet.isDisplayGridlines()).isFalse();
            assertThat(sheet.getRow(0).getCell(0).getStringCellValue()).isEqualTo("사용자 관리");
            assertThat(sheet.getMergedRegion(0).formatAsString()).isEqualTo("A1:H1");
            assertThat(sheet.getRow(1).getCell(0).getStringCellValue()).isEqualTo("권한그룹");
            assertThat(sheet.getRow(1).getCell(1).getStringCellValue()).isEqualTo("전체");
            assertThat(sheet.getMergedRegion(1).formatAsString()).isEqualTo("B2:H2");
            assertThat(sheet.getRow(6).getCell(0).getStringCellValue()).isEqualTo("아이디");
            assertThat(sheet.getRow(7).getCell(1).getStringCellValue()).isEqualTo("초기 관리자");
            assertThat(sheet.getPaneInformation().isFreezePane()).isTrue();
            assertThat(sheet.getPaneInformation().getHorizontalSplitPosition()).isEqualTo((short) 7);
            assertThat(sheet.getCTWorksheet().getAutoFilter().getRef()).isEqualTo("A7:H9");
            assertThat(sheet.getColumnWidth(0)).isEqualTo(18 * 256);
            assertThat(sheet.getColumnWidth(2)).isEqualTo(30 * 256);
            assertThat(sheet.getRow(0).getCell(0).getCellStyle().getFillForegroundColor())
                    .isEqualTo(IndexedColors.DARK_BLUE.getIndex());
            assertThat(sheet.getRow(6).getCell(0).getCellStyle().getFillForegroundColor())
                    .isEqualTo(IndexedColors.BLUE_GREY.getIndex());
            assertThat(sheet.getRow(7).getCell(3).getCellStyle().getAlignment())
                    .isEqualTo(HorizontalAlignment.CENTER);
            assertThat(sheet.getRow(8).getCell(0).getCellStyle().getFillForegroundColor())
                    .isEqualTo(IndexedColors.GREY_25_PERCENT.getIndex());
        }
    }

    @Test
    void 이력_문서의_처리결과를_성공과_실패_양식으로_구분한다() throws IOException {
        byte[] bytes = writer.write(new ExcelDocument(
                "로그인 이력",
                "로그인/인증 이력 조회",
                "조회된 로그인/인증 이력이 없습니다.",
                "이력 Excel 파일 생성에 실패했습니다.",
                List.of(
                        column("접속일시", 20, ExcelAlignment.CENTER),
                        column("이름", 16, ExcelAlignment.LEFT),
                        column("아이디", 18, ExcelAlignment.LEFT),
                        column("인증단계", 18, ExcelAlignment.CENTER),
                        resultColumn("처리결과", 14),
                        column("접속사유", 26, ExcelAlignment.LEFT),
                        column("실패사유", 24, ExcelAlignment.LEFT),
                        column("접속IP", 18, ExcelAlignment.CENTER),
                        column("User-Agent", 40, ExcelAlignment.LEFT)
                ),
                List.of(new ExcelSummary("조회 기간", "전체")),
                List.of(
                        List.of("2026-08-11 15:00:00", "관리자", "master", "OTP", "성공", "업무", "", "127.0.0.1", "Chrome"),
                        List.of("2026-08-11 14:00:00", "관리자", "master", "OTP", "실패", "업무", "INVALID_OTP", "127.0.0.1", "Chrome")
                )
        ));

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            XSSFSheet sheet = workbook.getSheetAt(0);

            assertThat(sheet.getRow(4).getCell(4).getCellStyle().getFillForegroundColor())
                    .isEqualTo(IndexedColors.LIGHT_GREEN.getIndex());
            assertThat(sheet.getRow(5).getCell(4).getCellStyle().getFillForegroundColor())
                    .isEqualTo(IndexedColors.ROSE.getIndex());
            assertThat(workbook.getFontAt(sheet.getRow(4).getCell(4).getCellStyle().getFontIndex()).getColor())
                    .isEqualTo(IndexedColors.DARK_GREEN.getIndex());
            assertThat(workbook.getFontAt(sheet.getRow(5).getCell(4).getCellStyle().getFontIndex()).getColor())
                    .isEqualTo(IndexedColors.DARK_RED.getIndex());
        }
    }

    @Test
    void 조회_결과가_없으면_문서의_안내_문구를_병합해_표시한다() throws IOException {
        byte[] bytes = writer.write(new ExcelDocument(
                "파일 이력",
                "파일 이력 조회",
                "조회된 파일 이력이 없습니다.",
                "이력 Excel 파일 생성에 실패했습니다.",
                List.of(
                        column("처리일시", 20, ExcelAlignment.CENTER),
                        column("아이디", 18, ExcelAlignment.LEFT),
                        column("접속IP", 18, ExcelAlignment.CENTER),
                        column("구분", 12, ExcelAlignment.CENTER),
                        column("파일명", 32, ExcelAlignment.LEFT),
                        column("용량(KB)", 14, ExcelAlignment.CENTER),
                        column("메뉴", 20, ExcelAlignment.LEFT),
                        resultColumn("처리결과", 14),
                        column("실패사유", 28, ExcelAlignment.LEFT)
                ),
                List.of(),
                List.of()
        ));

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            XSSFSheet sheet = workbook.getSheetAt(0);

            assertThat(sheet.getRow(3).getCell(0).getStringCellValue()).isEqualTo("조회된 파일 이력이 없습니다.");
            assertThat(sheet.getMergedRegion(sheet.getNumMergedRegions() - 1).formatAsString()).isEqualTo("A4:I4");
            assertThat(workbook.getFontAt(sheet.getRow(3).getCell(0).getCellStyle().getFontIndex()).getItalic())
                    .isTrue();
        }
    }

    private ExcelDocument.Column column(String header, int width, ExcelAlignment alignment) {
        return new ExcelDocument.Column(header, width, alignment, List.of());
    }

    private ExcelDocument.Column resultColumn(String header, int width) {
        return new ExcelDocument.Column(header, width, ExcelAlignment.CENTER, List.of("실패"));
    }
}
