package com.espay.admincore.adapter.out.file.excel;

import java.util.List;
import java.util.Set;

/**
 * 관리자 사용자 목록 Excel의 시트와 컬럼 표시 규칙.
 */
enum UserExcelTemplate implements ExcelSheetTemplate {

    USERS(
            "users",
            "사용자 관리",
            List.of("아이디", "이름", "이메일", "휴대폰번호", "부서", "권한", "상태", "최종 로그인"),
            new int[]{18, 16, 30, 18, 18, 18, 14, 22},
            "조회된 사용자가 없습니다."
    );

    private final String sheetName;
    private final String title;
    private final List<String> headers;
    private final int[] columnWidths;
    private final String noDataMessage;

    /**
     * 사용자 Excel 문서의 고정 표시 규칙을 구성한다.
     *
     * @param sheetName 시트 이름
     * @param title 문서 제목
     * @param headers 컬럼 헤더 목록
     * @param columnWidths 컬럼 너비 배열
     * @param noDataMessage 빈 결과 안내 문구
     */
    UserExcelTemplate(
            String sheetName,
            String title,
            List<String> headers,
            int[] columnWidths,
            String noDataMessage
    ) {
        this.sheetName = sheetName;
        this.title = title;
        this.headers = headers;
        this.columnWidths = columnWidths.clone();
        this.noDataMessage = noDataMessage;
    }

    /**
     * 사용자 목록을 생성할 시트 이름을 반환한다.
     *
     * @return 사용자 시트 이름
     */
    @Override
    public String sheetName() {
        return sheetName;
    }

    /**
     * 시트 최상단에 표시할 사용자 관리 제목을 반환한다.
     *
     * @return 사용자 문서 제목
     */
    @Override
    public String title() {
        return title;
    }

    /**
     * 사용자 본문 데이터 순서에 맞는 헤더 목록을 반환한다.
     *
     * @return 사용자 컬럼 헤더 목록
     */
    @Override
    public List<String> headers() {
        return headers;
    }

    /**
     * 사용자 헤더 순서에 맞는 컬럼 너비의 복사본을 반환한다.
     *
     * @return 사용자 컬럼 너비 배열
     */
    @Override
    public int[] columnWidths() {
        return columnWidths.clone();
    }

    /**
     * 조회된 사용자가 없을 때 표시할 문구를 반환한다.
     *
     * @return 사용자 빈 결과 안내 문구
     */
    @Override
    public String noDataMessage() {
        return noDataMessage;
    }

    /**
     * 휴대폰번호, 상태와 최종 로그인 시각을 가운데 정렬한다.
     *
     * @return 사용자 본문 스타일 정책
     */
    @Override
    public ExcelStylePolicy stylePolicy() {
        return ExcelStylePolicy.basic(Set.of(3, 6, 7));
    }

    /**
     * 사용자 문서 생성 실패 시 사용할 예외 메시지를 반환한다.
     *
     * @return 사용자 문서 생성 실패 메시지
     */
    @Override
    public String generationErrorMessage() {
        return "사용자 Excel 파일 생성에 실패했습니다.";
    }
}
