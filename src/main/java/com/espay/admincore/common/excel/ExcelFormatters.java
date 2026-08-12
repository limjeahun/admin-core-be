package com.espay.admincore.common.excel;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * 여러 Excel 다운로드 유스케이스에서 재사용하는 표시값 변환 함수.
 */
public final class ExcelFormatters {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private ExcelFormatters() {
    }

    public static String text(String value) {
        return value == null ? "" : value;
    }

    public static String date(LocalDate value) {
        return value == null ? "" : DATE.format(value);
    }

    public static String dateTime(LocalDateTime value) {
        return value == null ? "" : DATE_TIME.format(value);
    }

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

    public static String fileSize(Long value) {
        return value == null ? "" : String.format(Locale.KOREA, "%,dKB", value);
    }
}
