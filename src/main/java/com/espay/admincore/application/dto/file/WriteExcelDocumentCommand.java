package com.espay.admincore.application.dto.file;

import java.util.List;

/**
 * 표 형식 데이터를 Excel 문서 바이트로 생성하도록 요청하는 명령.
 *
 * @param sheetName 워크시트 이름
 * @param headers 열 제목 목록
 * @param rows 순서가 보존된 행 데이터
 */
public record WriteExcelDocumentCommand(String sheetName, List<String> headers,
                                        List<? extends List<?>> rows) {
    /**
     * 워크시트명과 표 데이터로 명령을 생성한다.
     * @param sheetName 워크시트 이름
     * @param headers 열 제목 목록
     * @param rows 순서가 보존된 행 데이터
     * @return Excel 문서 생성 명령
     */
    public static WriteExcelDocumentCommand of(String sheetName, List<String> headers,
                                                List<? extends List<?>> rows) {
        return new WriteExcelDocumentCommand(sheetName, headers, rows);
    }
}
