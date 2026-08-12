package com.espay.admincore.application.service.history;

import com.espay.admincore.application.dto.history.DownloadHistoryExcelCommand;
import com.espay.admincore.application.dto.history.LoginHistoryQuery;
import com.espay.admincore.application.port.out.file.ExcelDocumentWriterPort;
import com.espay.admincore.application.port.out.history.FileHistoryPersistencePort;
import com.espay.admincore.application.port.out.history.LoginHistoryPersistencePort;
import com.espay.admincore.common.excel.ExcelDocument;
import com.espay.admincore.domain.model.file.FileHistory;
import com.espay.admincore.domain.model.history.LoginHistory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HistoryExportServiceTest {
    @Mock
    private LoginHistoryPersistencePort loginHistoryPort;
    @Mock
    private FileHistoryPersistencePort fileHistoryPort;
    @Mock
    private ExcelDocumentWriterPort writer;

    private HistoryExportService service;

    @BeforeEach
    void setUp() {
        service = new HistoryExportService(loginHistoryPort, fileHistoryPort, writer);
    }

    @Test
    void 로그인_이력과_검색조건을_로그인_이력_문서_데이터로_전달한다() {
        LoginHistoryQuery query = LoginHistoryQuery.of(
                LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 11), "OTP", false,
                "LOGIN_ID", "master", 0, 10
        );
        LoginHistory history = LoginHistory.reconstitute(
                "1", "7", "초기 관리자", "master", "OTP", false, "업무 처리", "INVALID_OTP",
                "master", "127.0.0.1", "Chrome", LocalDateTime.of(2026, 8, 11, 15, 30)
        );
        when(loginHistoryPort.findPage(any(LoginHistoryQuery.class))).thenReturn(List.of(history));
        when(writer.write(any(ExcelDocument.class))).thenReturn(new byte[]{1, 2, 3});

        service.downloadLoginHistoryExcel(DownloadHistoryExcelCommand.of(query, "7", "127.0.0.1"));

        ArgumentCaptor<ExcelDocument> documentCaptor =
                ArgumentCaptor.forClass(ExcelDocument.class);
        verify(writer).write(documentCaptor.capture());
        ExcelDocument document = documentCaptor.getValue();

        assertThat(document.sheetName()).isEqualTo("로그인 이력");
        assertThat(document.title()).isEqualTo("로그인/인증 이력 조회");
        assertThat(document.columns())
                .extracting(ExcelDocument.Column::header)
                .containsExactly(
                        "접속일시", "이름", "아이디", "인증단계", "처리결과",
                        "접속사유", "실패사유", "접속IP", "User-Agent"
                );
        assertThat(document.summaries())
                .extracting(summary -> summary.label() + "=" + summary.value())
                .contains(
                        "조회 기간=2026-08-01 ~ 2026-08-11",
                        "인증 단계=OTP",
                        "처리 결과=실패",
                        "조회 조건=아이디 / master"
                );
        assertThat(document.rows()).containsExactly(List.of(
                "2026-08-11 15:30:00", "초기 관리자", "master", "OTP", "실패", "업무 처리",
                "INVALID_OTP", "127.0.0.1", "Chrome"
        ));

        ArgumentCaptor<FileHistory> historyCaptor = ArgumentCaptor.forClass(FileHistory.class);
        verify(fileHistoryPort).save(historyCaptor.capture());
        assertThat(historyCaptor.getValue().isSuccess()).isTrue();
        assertThat(historyCaptor.getValue().getMenuCode()).isEqualTo("LOGIN_HISTORY");
    }
}
