package com.espay.admincore.application.dto.file;

import java.util.List;
import java.util.Objects;

/**
 * 업무별 표 데이터를 Excel 문서로 생성하도록 요청하는 출력 Port 명령.
 *
 * @param documentType 적용할 관리자 Excel 문서 종류
 * @param summaries 문서 상단에 표시할 조회 조건 요약
 * @param rows 템플릿 컬럼 순서에 맞춘 본문 행 데이터
 */
public record WriteExcelDocumentCommand(
        ExcelDocumentType documentType,
        List<ExcelSummaryItem> summaries,
        List<List<String>> rows
) {
    /**
     * 출력 어댑터에 전달할 문서 데이터를 불변 목록으로 보관한다.
     *
     * @param documentType 적용할 관리자 Excel 문서 종류
     * @param summaries 문서 상단에 표시할 조회 조건 요약
     * @param rows 템플릿 컬럼 순서에 맞춘 본문 행 데이터
     */
    public WriteExcelDocumentCommand {
        documentType = Objects.requireNonNull(documentType, "documentType");
        summaries = List.copyOf(summaries);
        rows = rows.stream().map(List::copyOf).toList();
    }

    /**
     * 문서 종류와 표시 데이터로 Excel 생성 명령을 생성한다.
     *
     * @param documentType 적용할 관리자 Excel 문서 종류
     * @param summaries 문서 상단에 표시할 조회 조건 요약
     * @param rows 템플릿 컬럼 순서에 맞춘 본문 행 데이터
     * @return Excel 문서 생성 명령
     */
    public static WriteExcelDocumentCommand of(
            ExcelDocumentType documentType,
            List<ExcelSummaryItem> summaries,
            List<List<String>> rows
    ) {
        return new WriteExcelDocumentCommand(documentType, summaries, rows);
    }
}
