package com.espay.admincore.application.service.user;

import com.espay.admincore.application.dto.file.WriteExcelDocumentCommand;
import com.espay.admincore.application.dto.history.ExcelDownloadResult;
import com.espay.admincore.application.dto.user.DownloadUsersExcelCommand;
import com.espay.admincore.application.dto.user.UserQuery;
import com.espay.admincore.application.port.in.user.UserExportUseCase;
import com.espay.admincore.application.port.out.history.FileHistoryPersistencePort;
import com.espay.admincore.application.port.out.role.RolePersistencePort;
import com.espay.admincore.application.port.out.user.UserSearchPort;
import com.espay.admincore.application.port.out.file.ExcelDocumentWriterPort;
import com.espay.admincore.domain.model.file.FileHistory;
import com.espay.admincore.domain.model.user.AdminUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 조건에 맞는 전체 사용자와 권한명을 Excel로 만들고 다운로드 감사 이력을 기록하는 서비스.
 */
@Service
@RequiredArgsConstructor
public class UserExportService implements UserExportUseCase {
    private static final String FILE_NAME = "users.xlsx";
    private final UserSearchPort userSearchPort;
    private final RolePersistencePort rolePersistencePort;
    private final ExcelDocumentWriterPort writer;
    private final FileHistoryPersistencePort fileHistoryPersistencePort;

    /**
     * 사용자를 100건씩 전체 조회해 Excel을 생성하고 성공·실패 파일 이력을 남긴다.
     *
     * @param command 사용자 검색 조건과 다운로드 사용자·IP
     * @return users.xlsx 파일명과 바이너리
     */
    @Override
    @Transactional
    public ExcelDownloadResult downloadUsersExcel(DownloadUsersExcelCommand command) {
        try {
            List<? extends List<?>> rows = allUsers(command.query()).stream().map(user -> (List<?>) List.of(
                    user.getLoginId(), user.getName(), user.getEmail(), text(user.getPhoneNo()), text(user.getDeptName()),
                    rolePersistencePort.findById(user.getRoleId()).map(role -> role.getName()).orElse(""),
                    user.getStatus().name(), user.getLastLoginAt() == null ? "" : user.getLastLoginAt()
            )).toList();
            byte[] bytes = writer.write(WriteExcelDocumentCommand.of("사용자", List.of(
                    "아이디", "이름", "이메일", "휴대폰번호", "부서", "권한", "상태", "최종 로그인"
            ), rows));
            fileHistoryPersistencePort.save(FileHistory.downloadSucceeded(
                    command.userId(), "USERS", FILE_NAME,
                    bytes.length, command.clientIp()));
            return new ExcelDownloadResult(FILE_NAME, bytes);
        } catch (RuntimeException exception) {
            fileHistoryPersistencePort.save(FileHistory.downloadFailed(
                    command.userId(), "USERS", FILE_NAME,
                    exception.getMessage(), command.clientIp()));
            throw exception;
        }
    }

    /**
     * 검색 결과가 끝날 때까지 페이지 크기 100으로 사용자를 반복 조회한다.
     *
     * @param source 원본 사용자 검색 조건
     * @return Excel에 포함할 전체 사용자 목록
     */
    private List<AdminUser> allUsers(UserQuery source) {
        List<AdminUser> result = new ArrayList<>();
        int page = 0;
        while (true) {
            UserQuery query = UserQuery.of(source.roleId(), source.status(), source.conditionType(),
                    source.keyword(), page, 100);
            var batch = userSearchPort.findPage(query);
            result.addAll(batch);
            if (batch.size() < 100) {
                return result;
            }
            page++;
        }
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
