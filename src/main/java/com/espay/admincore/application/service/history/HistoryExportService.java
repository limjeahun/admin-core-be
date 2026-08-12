package com.espay.admincore.application.service.history;

import com.espay.admincore.application.dto.file.ExcelDownloadResult;
import com.espay.admincore.application.dto.history.DownloadHistoryExcelCommand;
import com.espay.admincore.application.dto.history.FileHistoryQuery;
import com.espay.admincore.application.dto.history.LoginHistoryQuery;
import com.espay.admincore.application.port.in.history.HistoryExportUseCase;
import com.espay.admincore.application.port.out.file.ExcelDocumentWriterPort;
import com.espay.admincore.application.port.out.history.FileHistoryPersistencePort;
import com.espay.admincore.application.port.out.history.LoginHistoryPersistencePort;
import com.espay.admincore.common.excel.ExcelColumn;
import com.espay.admincore.common.excel.ExcelDocument;
import com.espay.admincore.common.excel.ExcelFormatters;
import com.espay.admincore.common.excel.ExcelPipeline;
import com.espay.admincore.domain.model.file.FileHistory;
import com.espay.admincore.domain.model.history.LoginHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 인증·파일 이력을 전체 조회해 Excel 문서로 만들고 다운로드 감사 이력을 남기는 서비스.
 */
@Service
@RequiredArgsConstructor
public class HistoryExportService implements HistoryExportUseCase {
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
            LoginHistoryQuery query = command.query();
            List<LoginHistory> histories = allLoginHistory(query);
            byte[] bytes = writer.write(loginHistoryDocument(query, histories));
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
            FileHistoryQuery query = command.query();
            List<FileHistory> histories = allFileHistory(query);
            byte[] bytes = writer.write(fileHistoryDocument(query, histories));
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
     * 조회가 끝난 로그인 이력에 요약과 컬럼 정의를 적용해 Excel 문서를 생성한다.
     *
     * @param query 로그인 이력 검색 조건
     * @param histories Excel에 포함할 전체 로그인 이력
     * @return 출력 어댑터에 전달할 로그인 이력 Excel 문서
     */
    private ExcelDocument loginHistoryDocument(LoginHistoryQuery query, List<LoginHistory> histories) {
        return ExcelPipeline.from(histories)
                .sheetName("로그인 이력")
                .title("로그인/인증 이력 조회")
                .noDataMessage("조회된 로그인/인증 이력이 없습니다.")
                .generationErrorMessage("이력 Excel 파일 생성에 실패했습니다.")
                .summaryDateRange("조회 기간", query.fromDate(), query.toDate())
                .summary("인증 단계", authStepCondition(query.authStep()))
                .summary("처리 결과", resultCondition(query.success()))
                .summary("조회 조건", searchCondition(query.conditionType(), query.keyword()))
                .summaryNow("다운로드 일시")
                .column(ExcelColumn.dateTime("접속일시", 20, LoginHistory::getCreatedAt).centered())
                .column(ExcelColumn.text("이름", 16, LoginHistory::getUserName))
                .column(ExcelColumn.text("아이디", 18, this::displayId))
                .column(ExcelColumn.formatted("인증단계", 18, LoginHistory::getAuthStep, this::authStep).centered())
                .column(ExcelColumn.formatted("처리결과", 14, LoginHistory::isSuccess, this::result)
                        .centered()
                        .highlightFailures("실패"))
                .column(ExcelColumn.text("접속사유", 26, LoginHistory::getLoginReason))
                .column(ExcelColumn.text("실패사유", 24, LoginHistory::getFailReason))
                .column(ExcelColumn.text("접속IP", 18, LoginHistory::getClientIp).centered())
                .column(ExcelColumn.text("User-Agent", 40, LoginHistory::getUserAgent))
                .build();
    }

    /**
     * 조회가 끝난 파일 이력에 요약과 컬럼 정의를 적용해 Excel 문서를 생성한다.
     *
     * @param query 파일 이력 검색 조건
     * @param histories Excel에 포함할 전체 파일 이력
     * @return 출력 어댑터에 전달할 파일 이력 Excel 문서
     */
    private ExcelDocument fileHistoryDocument(FileHistoryQuery query, List<FileHistory> histories) {
        return ExcelPipeline.from(histories)
                .sheetName("파일 이력")
                .title("파일 이력 조회")
                .noDataMessage("조회된 파일 이력이 없습니다.")
                .generationErrorMessage("이력 Excel 파일 생성에 실패했습니다.")
                .summaryDateRange("조회 기간", query.fromDate(), query.toDate())
                .summary("구분", ioTypeCondition(query.ioType()))
                .summary("처리 결과", resultCondition(query.success()))
                .summary("조회 조건", searchCondition(query.conditionType(), query.keyword()))
                .summaryNow("다운로드 일시")
                .column(ExcelColumn.dateTime("처리일시", 20, FileHistory::getCreatedAt).centered())
                .column(ExcelColumn.text("아이디", 18, FileHistory::getLoginId))
                .column(ExcelColumn.text("접속IP", 18, FileHistory::getClientIp).centered())
                .column(ExcelColumn.formatted("구분", 12, FileHistory::getIoType, this::ioType).centered())
                .column(ExcelColumn.text("파일명", 32, FileHistory::getFileName))
                .column(ExcelColumn.fileSize("용량(KB)", 14, FileHistory::getFileSize).centered())
                .column(ExcelColumn.text("메뉴", 20, FileHistory::getMenuName))
                .column(ExcelColumn.formatted("처리결과", 14, FileHistory::isSuccess, this::result)
                        .centered()
                        .highlightFailures("실패"))
                .column(ExcelColumn.text("실패사유", 28, FileHistory::getFailReason))
                .build();
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
        return history.getLoginId() == null
                ? ExcelFormatters.text(history.getInputId())
                : history.getLoginId();
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
     * 문자열에 공백이 아닌 내용이 있는지 확인한다.
     */
    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
