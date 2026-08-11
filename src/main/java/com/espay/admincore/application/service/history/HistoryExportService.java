package com.espay.admincore.application.service.history;

import com.espay.admincore.application.dto.file.ExcelDocumentType;
import com.espay.admincore.application.dto.file.ExcelDownloadResult;
import com.espay.admincore.application.dto.file.ExcelSummaryItem;
import com.espay.admincore.application.dto.file.WriteExcelDocumentCommand;
import com.espay.admincore.application.dto.history.DownloadHistoryExcelCommand;
import com.espay.admincore.application.dto.history.FileHistoryQuery;
import com.espay.admincore.application.dto.history.LoginHistoryQuery;
import com.espay.admincore.application.port.in.history.HistoryExportUseCase;
import com.espay.admincore.application.port.out.file.ExcelDocumentWriterPort;
import com.espay.admincore.application.port.out.history.FileHistoryPersistencePort;
import com.espay.admincore.application.port.out.history.LoginHistoryPersistencePort;
import com.espay.admincore.domain.model.file.FileHistory;
import com.espay.admincore.domain.model.history.LoginHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 인증·파일 이력을 전체 조회해 Excel 문서로 만들고 다운로드 감사 이력을 남기는 서비스.
 */
@Service
@RequiredArgsConstructor
public class HistoryExportService implements HistoryExportUseCase {
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final LoginHistoryPersistencePort loginHistoryPort;
    private final FileHistoryPersistencePort fileHistoryPort;
    private final ExcelDocumentWriterPort writer;

    /**
     * 조건에 맞는 모든 로그인·OTP 이력을 100건씩 조회해 Excel 문서로 생성한다.
     *
     * <p>생성 성공과 실패 모두 LOGIN_HISTORY 메뉴의 다운로드 감사 이력으로 기록한다.</p>
     *
     * @param command 로그인 이력 검색 조건과 다운로드 사용자·IP
     * @return 고정 파일명과 생성된 Excel 바이너리
     */
    @Override
    @Transactional
    public ExcelDownloadResult downloadLoginHistoryExcel(DownloadHistoryExcelCommand<LoginHistoryQuery> command) {
        String fileName = "login-history.xlsx";
        try {
            List<List<String>> rows = allLoginHistory(command.query()).stream()
                    .map(this::toLoginHistoryExcelRow)
                    .toList();
            byte[] bytes = writer.write(WriteExcelDocumentCommand.of(
                    ExcelDocumentType.LOGIN_HISTORY,
                    loginHistorySummaries(command.query()),
                    rows
            ));
            fileHistoryPort.save(FileHistory.downloadSucceeded(
                    command.userId(), "LOGIN_HISTORY", fileName,
                    bytes.length, command.clientIp()));
            return new ExcelDownloadResult(fileName, bytes);
        } catch (RuntimeException exception) {
            fileHistoryPort.save(FileHistory.downloadFailed(
                    command.userId(), "LOGIN_HISTORY", fileName,
                    exception.getMessage(), command.clientIp()));
            throw exception;
        }
    }

    /**
     * 조건에 맞는 모든 파일 이력을 100건씩 조회해 Excel 문서로 생성한다.
     *
     * <p>생성 성공과 실패 모두 FILE_HISTORY 메뉴의 다운로드 감사 이력으로 기록한다.</p>
     *
     * @param command 파일 이력 검색 조건과 다운로드 사용자·IP
     * @return 고정 파일명과 생성된 Excel 바이너리
     */
    @Override
    @Transactional
    public ExcelDownloadResult downloadFileHistoryExcel(DownloadHistoryExcelCommand<FileHistoryQuery> command) {
        String fileName = "file-history.xlsx";
        try {
            List<List<String>> rows = allFileHistory(command.query()).stream()
                    .map(this::toFileHistoryExcelRow)
                    .toList();
            byte[] bytes = writer.write(WriteExcelDocumentCommand.of(
                    ExcelDocumentType.FILE_HISTORY,
                    fileHistorySummaries(command.query()),
                    rows
            ));
            fileHistoryPort.save(FileHistory.downloadSucceeded(
                    command.userId(), "FILE_HISTORY", fileName,
                    bytes.length, command.clientIp()));
            return new ExcelDownloadResult(fileName, bytes);
        } catch (RuntimeException exception) {
            fileHistoryPort.save(FileHistory.downloadFailed(
                    command.userId(), "FILE_HISTORY", fileName,
                    exception.getMessage(), command.clientIp()));
            throw exception;
        }
    }

    /**
     * 로그인 이력을 로그인 이력 Excel 템플릿의 컬럼 순서에 맞춘 행으로 변환한다.
     */
    private List<String> toLoginHistoryExcelRow(LoginHistory history) {
        return List.of(
                dateTime(history.getCreatedAt()),
                text(history.getUserName()),
                displayId(history),
                authStep(history.getAuthStep()),
                result(history.isSuccess()),
                text(history.getLoginReason()),
                text(history.getFailReason()),
                text(history.getClientIp()),
                text(history.getUserAgent())
        );
    }

    /**
     * 파일 이력을 파일 이력 Excel 템플릿의 컬럼 순서에 맞춘 행으로 변환한다.
     */
    private List<String> toFileHistoryExcelRow(FileHistory history) {
        return List.of(
                dateTime(history.getCreatedAt()),
                text(history.getLoginId()),
                text(history.getClientIp()),
                ioType(history.getIoType()),
                text(history.getFileName()),
                fileSize(history.getFileSize()),
                text(history.getMenuName()),
                result(history.isSuccess()),
                text(history.getFailReason())
        );
    }

    /**
     * 로그인 이력 검색 조건과 다운로드 시각을 문서 상단 요약값으로 만든다.
     */
    private List<ExcelSummaryItem> loginHistorySummaries(LoginHistoryQuery query) {
        return List.of(
                ExcelSummaryItem.of("조회 기간", dateRange(query.fromDate(), query.toDate())),
                ExcelSummaryItem.of("인증 단계", authStepCondition(query.authStep())),
                ExcelSummaryItem.of("처리 결과", resultCondition(query.success())),
                ExcelSummaryItem.of("조회 조건", searchCondition(query.conditionType(), query.keyword())),
                ExcelSummaryItem.of("다운로드 일시", DATE_TIME.format(LocalDateTime.now()))
        );
    }

    /**
     * 파일 이력 검색 조건과 다운로드 시각을 문서 상단 요약값으로 만든다.
     */
    private List<ExcelSummaryItem> fileHistorySummaries(FileHistoryQuery query) {
        return List.of(
                ExcelSummaryItem.of("조회 기간", dateRange(query.fromDate(), query.toDate())),
                ExcelSummaryItem.of("구분", ioTypeCondition(query.ioType())),
                ExcelSummaryItem.of("처리 결과", resultCondition(query.success())),
                ExcelSummaryItem.of("조회 조건", searchCondition(query.conditionType(), query.keyword())),
                ExcelSummaryItem.of("다운로드 일시", DATE_TIME.format(LocalDateTime.now()))
        );
    }

    /**
     * 검색 결과가 끝날 때까지 로그인 이력을 페이지 크기 100으로 반복 조회한다.
     *
     * @param source 원본 검색 조건
     * @return Excel에 포함할 전체 로그인 이력
     */
    private List<LoginHistory> allLoginHistory(LoginHistoryQuery source) {
        List<LoginHistory> result = new ArrayList<>();
        for (int page = 0; ; page++) {
            var query = LoginHistoryQuery.of(source.fromDate(), source.toDate(), source.authStep(), source.success(),
                    source.conditionType(), source.keyword(), page, 100);
            var batch = loginHistoryPort.findPage(query);
            result.addAll(batch);
            if (batch.size() < 100) return result;
        }
    }

    /**
     * 검색 결과가 끝날 때까지 파일 이력을 페이지 크기 100으로 반복 조회한다.
     *
     * @param source 원본 검색 조건
     * @return Excel에 포함할 전체 파일 이력
     */
    private List<FileHistory> allFileHistory(FileHistoryQuery source) {
        List<FileHistory> result = new ArrayList<>();
        for (int page = 0; ; page++) {
            var query = FileHistoryQuery.of(source.fromDate(), source.toDate(), source.ioType(), source.success(),
                    source.conditionType(), source.keyword(), page, 100);
            var batch = fileHistoryPort.findPage(query);
            result.addAll(batch);
            if (batch.size() < 100) return result;
        }
    }

    /**
     * 식별된 로그인 ID가 있으면 사용하고, 없으면 실제 입력 ID를 표시한다.
     *
     * @param history 로그인 이력
     * @return Excel에 표시할 로그인 ID
     */
    private String displayId(LoginHistory history) {
        return history.getLoginId() == null ? text(history.getInputId()) : history.getLoginId();
    }

    /**
     * 인증 단계 코드를 Excel 표시값으로 변환한다.
     */
    private String authStep(String value) {
        if (!hasText(value)) {
            return "";
        }
        return switch (value.trim().toUpperCase(Locale.ROOT)) {
            case "LOGIN" -> "아이디·비밀번호";
            case "OTP" -> "OTP";
            default -> value;
        };
    }

    /**
     * 인증 단계 검색 조건을 상단 요약에 표시할 문구로 변환한다.
     */
    private String authStepCondition(String value) {
        return hasText(value) ? authStep(value) : "전체";
    }

    /**
     * 파일 처리 구분 코드를 Excel 표시값으로 변환한다.
     */
    private String ioType(String value) {
        if (!hasText(value)) {
            return "";
        }
        return value.trim().toUpperCase(Locale.ROOT).startsWith("U") ? "업로드" : "다운로드";
    }

    /**
     * 파일 처리 구분 검색 조건을 상단 요약에 표시할 문구로 변환한다.
     */
    private String ioTypeCondition(String value) {
        return hasText(value) ? ioType(value) : "전체";
    }

    /**
     * 처리 성공 여부를 Excel 표시값으로 변환한다.
     */
    private String result(boolean success) {
        return success ? "성공" : "실패";
    }

    /**
     * 처리 결과 검색 조건을 상단 요약에 표시할 문구로 변환한다.
     */
    private String resultCondition(Boolean success) {
        return success == null ? "전체" : result(success);
    }

    /**
     * 조회 시작일과 종료일을 하나의 기간 표시값으로 변환한다.
     */
    private String dateRange(LocalDate fromDate, LocalDate toDate) {
        if (fromDate == null && toDate == null) {
            return "전체";
        }
        if (fromDate == null) {
            return "~ " + DATE.format(toDate);
        }
        if (toDate == null) {
            return DATE.format(fromDate) + " ~";
        }
        return DATE.format(fromDate) + " ~ " + DATE.format(toDate);
    }

    /**
     * 검색 필드와 키워드를 상단 요약에 표시할 문구로 변환한다.
     */
    private String searchCondition(String conditionType, String keyword) {
        if (!hasText(keyword)) {
            return "전체";
        }
        String label = !hasText(conditionType) ? "전체" : switch (conditionType.trim().toUpperCase()) {
            case "NAME", "USER_NAME" -> "이름";
            case "LOGIN_ID", "ID" -> "아이디";
            case "IP", "CLIENT_IP" -> "접속IP";
            case "FILE_NAME" -> "파일명";
            default -> "전체";
        };
        return label + " / " + keyword.trim();
    }

    /**
     * 파일 크기를 천 단위 구분자가 포함된 KB 값으로 변환한다.
     */
    private String fileSize(Long value) {
        return value == null ? "" : String.format(Locale.KOREA, "%,dKB", value);
    }

    /**
     * 시각을 Excel 표시 형식으로 변환한다.
     */
    private String dateTime(LocalDateTime value) {
        return value == null ? "" : DATE_TIME.format(value);
    }

    /**
     * Excel 셀에 {@code null} 대신 빈 문자열을 기록한다.
     *
     * @param value 원본 문자열
     * @return 원본 문자열 또는 빈 문자열
     */
    private String text(String value) {
        return value == null ? "" : value;
    }

    /**
     * 문자열에 공백이 아닌 내용이 있는지 확인한다.
     */
    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
