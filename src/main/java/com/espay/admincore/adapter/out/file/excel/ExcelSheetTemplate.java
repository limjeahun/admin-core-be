package com.espay.admincore.adapter.out.file.excel;

import java.util.List;

/**
 * 출력 어댑터가 Excel 시트를 그릴 때 사용하는 문서별 표시 규칙.
 */
interface ExcelSheetTemplate {

    /**
     * 워크북에 생성할 시트 이름을 반환한다.
     *
     * @return Excel 시트 이름
     */
    String sheetName();

    /**
     * 시트 최상단에 표시할 문서 제목을 반환한다.
     *
     * @return 문서 제목
     */
    String title();

    /**
     * 본문 컬럼 순서에 맞는 헤더 목록을 반환한다.
     *
     * @return 컬럼 헤더 목록
     */
    List<String> headers();

    /**
     * 헤더 순서에 맞는 컬럼 너비를 반환한다.
     *
     * @return 문자 단위 컬럼 너비 배열
     */
    int[] columnWidths();

    /**
     * 본문 데이터가 없을 때 표시할 문구를 반환한다.
     *
     * @return 빈 결과 안내 문구
     */
    String noDataMessage();

    /**
     * 본문 컬럼의 정렬과 결과 강조 규칙을 반환한다.
     *
     * @return 본문 스타일 정책
     */
    ExcelStylePolicy stylePolicy();

    /**
     * XLSX 생성에 실패했을 때 사용할 예외 메시지를 반환한다.
     *
     * @return 문서 생성 실패 메시지
     */
    String generationErrorMessage();
}
