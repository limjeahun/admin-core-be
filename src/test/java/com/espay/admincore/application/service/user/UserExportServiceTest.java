package com.espay.admincore.application.service.user;

import com.espay.admincore.application.dto.user.DownloadUsersExcelCommand;
import com.espay.admincore.application.dto.user.UserQuery;
import com.espay.admincore.application.exception.ExcelDocumentWriteException;
import com.espay.admincore.application.port.out.file.ExcelDocumentWriterPort;
import com.espay.admincore.application.port.out.history.FileHistoryPersistencePort;
import com.espay.admincore.application.port.out.role.RolePersistencePort;
import com.espay.admincore.application.port.out.user.UserSearchPort;
import com.espay.admincore.common.excel.ExcelDocument;
import com.espay.admincore.domain.model.file.FileHistory;
import com.espay.admincore.domain.model.role.AdminRole;
import com.espay.admincore.domain.model.user.AdminUser;
import com.espay.admincore.domain.model.user.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserExportServiceTest {
    @Mock
    private UserSearchPort userSearchPort;
    @Mock
    private RolePersistencePort rolePersistencePort;
    @Mock
    private ExcelDocumentWriterPort writer;
    @Mock
    private FileHistoryPersistencePort fileHistoryPersistencePort;

    private UserExportService service;

    @BeforeEach
    void setUp() {
        service = new UserExportService(
                userSearchPort,
                rolePersistencePort,
                writer,
                fileHistoryPersistencePort
        );
    }

    @Test
    void 사용자_조회_결과와_검색조건을_사용자_문서_데이터로_전달한다() {
        UserQuery query = UserQuery.of("1", "ACTIVE", "NAME", "관리자", 0, 10);
        LocalDateTime createdAt = LocalDateTime.of(2026, 8, 1, 9, 0);
        LocalDateTime lastLoginAt = LocalDateTime.of(2026, 8, 11, 15, 30);
        AdminUser user = AdminUser.reconstitute(
                "7", "master", "초기 관리자", "admin@example.com", "010-0000-0000", "운영",
                "1", "password-hash", null, lastLoginAt, UserStatus.ACTIVE, createdAt, createdAt
        );
        AdminRole role = AdminRole.reconstitute("1", "MASTER", "마스터 권한", true, createdAt, createdAt);
        when(userSearchPort.findPage(any(UserQuery.class))).thenReturn(List.of(user));
        when(rolePersistencePort.findById("1")).thenReturn(Optional.of(role));
        when(writer.write(any(ExcelDocument.class))).thenReturn(new byte[]{1, 2, 3});

        service.downloadUsersExcel(DownloadUsersExcelCommand.of(query, "7", "127.0.0.1"));

        ArgumentCaptor<ExcelDocument> documentCaptor =
                ArgumentCaptor.forClass(ExcelDocument.class);
        verify(writer).write(documentCaptor.capture());
        ExcelDocument document = documentCaptor.getValue();

        assertThat(document.sheetName()).isEqualTo("users");
        assertThat(document.title()).isEqualTo("사용자 관리");
        assertThat(document.columns())
                .extracting(ExcelDocument.Column::header)
                .containsExactly("아이디", "이름", "이메일", "휴대폰번호", "부서", "권한", "상태", "최종 로그인");
        assertThat(document.summaries())
                .extracting(summary -> summary.label() + "=" + summary.value())
                .contains("권한그룹=MASTER", "사용여부=이용중", "조회조건=이름 / 관리자");
        assertThat(document.rows()).containsExactly(List.of(
                "master", "초기 관리자", "admin@example.com", "010-0000-0000", "운영", "MASTER", "이용중",
                "2026-08-11 15:30:00"
        ));

        ArgumentCaptor<FileHistory> historyCaptor = ArgumentCaptor.forClass(FileHistory.class);
        verify(fileHistoryPersistencePort).save(historyCaptor.capture());
        assertThat(historyCaptor.getValue().isSuccess()).isTrue();
        assertThat(historyCaptor.getValue().getMenuCode()).isEqualTo("USERS");
    }

    @Test
    void Excel_출력_실패를_감사_이력에_기록하고_예외를_전파한다() {
        UserQuery query = UserQuery.of(null, null, null, null, 0, 10);
        ExcelDocumentWriteException writeException =
                new ExcelDocumentWriteException(new IOException("write failed"));
        when(userSearchPort.findPage(any(UserQuery.class))).thenReturn(List.of());
        when(writer.write(any(ExcelDocument.class))).thenThrow(writeException);

        assertThatThrownBy(() -> service.downloadUsersExcel(
                DownloadUsersExcelCommand.of(query, "7", "127.0.0.1")
        )).isSameAs(writeException);

        ArgumentCaptor<FileHistory> historyCaptor = ArgumentCaptor.forClass(FileHistory.class);
        verify(fileHistoryPersistencePort).save(historyCaptor.capture());
        assertThat(historyCaptor.getValue().isSuccess()).isFalse();
        assertThat(historyCaptor.getValue().getFailReason()).isEqualTo("Excel 문서를 생성하지 못했습니다.");
    }
}
