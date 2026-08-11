package com.espay.admincore.application.service.history;

import com.espay.admincore.application.dto.file.WriteExcelDocumentCommand;
import com.espay.admincore.application.dto.history.*;
import com.espay.admincore.application.port.in.history.HistoryExportUseCase;
import com.espay.admincore.application.port.out.history.FileHistoryPersistencePort;
import com.espay.admincore.application.port.out.history.LoginHistoryPersistencePort;
import com.espay.admincore.application.port.out.file.ExcelDocumentWriterPort;
import com.espay.admincore.domain.model.file.FileHistory;
import com.espay.admincore.domain.model.history.LoginHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

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
            List<? extends List<?>> rows = allLoginHistory(command.query()).stream().map(history -> (List<?>) List.of(
                    history.getCreatedAt(), text(history.getUserName()), displayId(history), history.getAuthStep(),
                    history.isSuccess() ? "성공" : "실패", text(history.getLoginReason()), text(history.getFailReason()),
                    text(history.getClientIp()), text(history.getUserAgent())
            )).toList();
            byte[] bytes = writer.write(WriteExcelDocumentCommand.of("로그인 이력", List.of(
                    "접속일시", "이름", "아이디", "인증단계", "처리결과", "접속사유", "실패사유", "접속IP", "User-Agent"
            ), rows));
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
            List<? extends List<?>> rows = allFileHistory(command.query()).stream().map(history -> (List<?>) List.of(
                    history.getCreatedAt(), text(history.getLoginId()), text(history.getClientIp()),
                    "U".equals(history.getIoType()) ? "업로드" : "다운로드", history.getFileName(),
                    history.getFileSize() == null ? "" : history.getFileSize(), text(history.getMenuName()),
                    history.isSuccess() ? "성공" : "실패", text(history.getFailReason())
            )).toList();
            byte[] bytes = writer.write(WriteExcelDocumentCommand.of("파일 이력", List.of(
                    "처리일시", "아이디", "접속IP", "구분", "파일명", "용량(KB)", "메뉴", "처리결과", "실패사유"
            ), rows));
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
     * Excel 셀에 {@code null} 대신 빈 문자열을 기록한다.
     *
     * @param value 원본 문자열
     * @return 원본 문자열 또는 빈 문자열
     */
    private String text(String value) {
        return value == null ? "" : value;
    }
}
