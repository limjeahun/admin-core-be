package com.espay.admincore.adapter.out.file.excel;

import java.util.List;
import java.util.Set;

/**
 * 로그인·OTP 및 파일 감사 이력 Excel의 시트와 컬럼 표시 규칙.
 */
enum HistoryExcelTemplate implements ExcelSheetTemplate {

    LOGIN(
            "로그인 이력",
            "로그인/인증 이력 조회",
            List.of("접속일시", "이름", "아이디", "인증단계", "처리결과", "접속사유", "실패사유", "접속IP", "User-Agent"),
            new int[]{20, 16, 18, 18, 14, 26, 24, 18, 40},
            "조회된 로그인/인증 이력이 없습니다.",
            Set.of(0, 3, 4, 7),
            4
    ),
    FILE(
            "파일 이력",
            "파일 이력 조회",
            List.of("처리일시", "아이디", "접속IP", "구분", "파일명", "용량(KB)", "메뉴", "처리결과", "실패사유"),
            new int[]{20, 18, 18, 12, 32, 14, 20, 14, 28},
            "조회된 파일 이력이 없습니다.",
            Set.of(0, 2, 3, 5, 7),
            7
    );

    private final String sheetName;
    private final String title;
    private final List<String> headers;
    private final int[] columnWidths;
    private final String noDataMessage;
    private final Set<Integer> centerAlignedColumns;
    private final int resultColumnIndex;

    /**
     * 이력 Excel 문서의 고정 표시 규칙을 구성한다.
     *
     * @param sheetName 시트 이름
     * @param title 문서 제목
     * @param headers 컬럼 헤더 목록
     * @param columnWidths 컬럼 너비 배열
     * @param noDataMessage 빈 결과 안내 문구
     * @param centerAlignedColumns 가운데 정렬할 컬럼 인덱스
     * @param resultColumnIndex 성공·실패를 강조할 컬럼 인덱스
     */
    HistoryExcelTemplate(
            String sheetName,
            String title,
            List<String> headers,
            int[] columnWidths,
            String noDataMessage,
            Set<Integer> centerAlignedColumns,
            int resultColumnIndex
    ) {
        this.sheetName = sheetName;
        this.title = title;
        this.headers = headers;
        this.columnWidths = columnWidths.clone();
        this.noDataMessage = noDataMessage;
        this.centerAlignedColumns = Set.copyOf(centerAlignedColumns);
        this.resultColumnIndex = resultColumnIndex;
    }

    /**
     * 선택한 이력 문서의 시트 이름을 반환한다.
     *
     * @return 이력 시트 이름
     */
    @Override
    public String sheetName() {
        return sheetName;
    }

    /**
     * 시트 최상단에 표시할 이력 문서 제목을 반환한다.
     *
     * @return 이력 문서 제목
     */
    @Override
    public String title() {
        return title;
    }

    /**
     * 선택한 이력 본문 순서에 맞는 헤더 목록을 반환한다.
     *
     * @return 이력 컬럼 헤더 목록
     */
    @Override
    public List<String> headers() {
        return headers;
    }

    /**
     * 이력 헤더 순서에 맞는 컬럼 너비의 복사본을 반환한다.
     *
     * @return 이력 컬럼 너비 배열
     */
    @Override
    public int[] columnWidths() {
        return columnWidths.clone();
    }

    /**
     * 조회된 이력이 없을 때 표시할 문구를 반환한다.
     *
     * @return 이력 빈 결과 안내 문구
     */
    @Override
    public String noDataMessage() {
        return noDataMessage;
    }

    /**
     * 문서별 정렬 컬럼과 성공·실패 결과 컬럼을 적용한다.
     *
     * @return 이력 본문 스타일 정책
     */
    @Override
    public ExcelStylePolicy stylePolicy() {
        return ExcelStylePolicy.withResult(centerAlignedColumns, resultColumnIndex, List.of("실패"));
    }

    /**
     * 이력 문서 생성 실패 시 사용할 예외 메시지를 반환한다.
     *
     * @return 이력 문서 생성 실패 메시지
     */
    @Override
    public String generationErrorMessage() {
        return "이력 Excel 파일 생성에 실패했습니다.";
    }
}
