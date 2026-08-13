package com.espay.admincore.common.excel;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** 여러 Excel 다운로드 유스케이스에서 재사용하는 표시값 변환 함수. */
public final class ExcelFormatters {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 표시값 변환만 제공하도록 인스턴스 생성을 막는다.
     */
    private ExcelFormatters() {
    }

    /**
     * null 문자열을 빈 문자열로 변환한다.
     *
     * @param value 변환할 문자열
     * @return 원본 문자열 또는 빈 문자열
     */
    public static String text(String value) {
        return value == null ? "" : value;
    }

    /**
     * 날짜를 {@code yyyy-MM-dd} 형식으로 변환한다.
     *
     * @param value 변환할 날짜
     * @return 변환된 날짜 또는 빈 문자열
     */
    public static String date(LocalDate value) {
        return value == null ? "" : DATE.format(value);
    }

    /**
     * 날짜와 시각을 {@code yyyy-MM-dd HH:mm:ss} 형식으로 변환한다.
     *
     * @param value 변환할 날짜와 시각
     * @return 변환된 날짜와 시각 또는 빈 문자열
     */
    public static String dateTime(LocalDateTime value) {
        return value == null ? "" : DATE_TIME.format(value);
    }

    /**
     * 시작일과 종료일을 조회 기간 문자열로 변환한다.
     *
     * @param fromDate 조회 시작일
     * @param toDate 조회 종료일
     * @return 변환된 조회 기간 문자열
     */
    public static String dateRange(LocalDate fromDate, LocalDate toDate) {
        if (fromDate == null && toDate == null) {
            return "전체";
        }
        if (fromDate == null) {
            return "~ " + date(toDate);
        }
        if (toDate == null) {
            return date(fromDate) + " ~";
        }
        return date(fromDate) + " ~ " + date(toDate);
    }

    /**
     * KB 단위 파일 크기에 천 단위 구분자를 적용한다.
     *
     * @param value 변환할 KB 단위 파일 크기
     * @return 변환된 파일 크기 또는 빈 문자열
     */
    public static String fileSize(Long value) {
        return value == null ? "" : String.format(Locale.KOREA, "%,dKB", value);
    }
}
