package com.espay.admincore.application.service.user;

import com.espay.admincore.application.dto.file.ExcelDownloadResult;
import com.espay.admincore.application.dto.user.DownloadUsersExcelCommand;
import com.espay.admincore.application.dto.user.UserQuery;
import com.espay.admincore.application.port.in.user.UserExportUseCase;
import com.espay.admincore.application.port.out.file.ExcelDocumentWriterPort;
import com.espay.admincore.application.port.out.history.FileHistoryPersistencePort;
import com.espay.admincore.application.port.out.role.RolePersistencePort;
import com.espay.admincore.application.port.out.user.UserSearchPort;
import com.espay.admincore.common.excel.ExcelColumn;
import com.espay.admincore.common.excel.ExcelDocument;
import com.espay.admincore.common.excel.ExcelPipeline;
import com.espay.admincore.domain.model.file.FileHistory;
import com.espay.admincore.domain.model.user.AdminUser;
import com.espay.admincore.domain.model.user.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 조건에 맞는 전체 사용자와 권한명을 Excel로 만들고 다운로드 감사 이력을 기록하는 서비스.
 */
@Service
@RequiredArgsConstructor
public class UserExportService implements UserExportUseCase {
    private static final String FILE_NAME = "users.xlsx";

    private final UserSearchPort                userSearchPort;
    private final RolePersistencePort           rolePersistencePort;
    private final ExcelDocumentWriterPort       writer;
    private final FileHistoryPersistencePort    fileHistoryPersistencePort;

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
            UserQuery query = command.query();
            List<AdminUser> users = allUsers(query);
            byte[] bytes = writer.write(usersDocument(query, users));
            fileHistoryPersistencePort.save(
                    FileHistory.downloadSucceeded(
                            command.userId(),
                            "USERS",
                            FILE_NAME,
                            bytes.length,
                            command.clientIp()
                    )
            );
            return ExcelDownloadResult.of(FILE_NAME, bytes);
        } catch (RuntimeException exception) {
            fileHistoryPersistencePort.save(
                    FileHistory.downloadFailed(
                            command.userId(),
                            "USERS",
                            FILE_NAME,
                            exception.getMessage(),
                            command.clientIp()
                    )
            );
            throw exception;
        }
    }

    /**
     * 조회가 끝난 사용자 목록에 요약과 컬럼 정의를 적용해 Excel 문서를 생성한다.
     *
     * @param query 사용자 검색 조건
     * @param users Excel에 포함할 전체 사용자 목록
     * @return 출력 어댑터에 전달할 사용자 Excel 문서
     */
    private ExcelDocument usersDocument(UserQuery query, List<AdminUser> users) {
        Map<String, String> roleNames = new HashMap<>();

        return ExcelPipeline.from(users)
                .sheetName("users")
                .title("사용자 관리")
                .noDataMessage("조회된 사용자가 없습니다.")
                .generationErrorMessage("사용자 Excel 파일 생성에 실패했습니다.")
                .summary("권한그룹", roleCondition(query.roleId(), roleNames))
                .summary("사용여부", statusCondition(query.status()))
                .summary("조회조건", searchCondition(query.conditionType(), query.keyword()))
                .summaryNow("다운로드 일시")
                .column(ExcelColumn.text("아이디", 18, AdminUser::getLoginId))
                .column(ExcelColumn.text("이름", 16, AdminUser::getName))
                .column(ExcelColumn.text("이메일", 30, AdminUser::getEmail))
                .column(ExcelColumn.text("휴대폰번호", 18, AdminUser::getPhoneNo).centered())
                .column(ExcelColumn.text("부서", 18, AdminUser::getDeptName))
                .column(ExcelColumn.formatted("권한", 18, AdminUser::getRoleId,
                        roleId -> roleName(roleId, roleNames)))
                .column(ExcelColumn.formatted("상태", 14, AdminUser::getStatus, this::status).centered())
                .column(ExcelColumn.dateTime("최종 로그인", 22, AdminUser::getLastLoginAt).centered())
                .build();
    }

    /**
     * 권한 ID를 표시명으로 변환하고 같은 다운로드 안에서는 조회 결과를 재사용한다.
     */
    private String roleName(String roleId, Map<String, String> roleNames) {
        if (!hasText(roleId)) {
            return "";
        }
        return roleNames.computeIfAbsent(roleId, id -> rolePersistencePort.findById(id)
                .map(role -> role.getName())
                .orElse(id));
    }

    /**
     * 권한 검색 조건을 상단 요약에 표시할 문구로 변환한다.
     */
    private String roleCondition(String roleId, Map<String, String> roleNames) {
        return hasText(roleId) ? roleName(roleId, roleNames) : "전체";
    }

    /**
     * 사용자 상태를 화면에서 읽기 쉬운 한글 값으로 변환한다.
     */
    private String status(UserStatus status) {
        return status == UserStatus.ACTIVE ? "이용중" : "이용중지";
    }

    /**
     * 사용자 상태 검색 조건을 상단 요약에 표시할 문구로 변환한다.
     */
    private String statusCondition(String status) {
        if (!hasText(status) || "ALL".equalsIgnoreCase(status) || "전체".equals(status)) {
            return "전체";
        }
        return UserStatus.from(status) == UserStatus.ACTIVE ? "이용중" : "이용중지";
    }

    /**
     * 검색 필드와 키워드를 상단 요약에 표시할 문구로 변환한다.
     */
    private String searchCondition(String conditionType, String keyword) {
        if (!hasText(keyword)) {
            return "전체";
        }
        String label = !hasText(conditionType) ? "전체" : switch (conditionType.trim().toUpperCase()) {
            case "LOGIN_ID", "ID" -> "아이디";
            case "NAME", "USER_NAME" -> "이름";
            case "EMAIL" -> "이메일";
            default -> "전체";
        };
        return label + " / " + keyword.trim();
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
            UserQuery query = UserQuery.of(
                    source.roleId(),
                    source.status(),
                    source.conditionType(),
                    source.keyword(),
                    page,
                    100
            );
            var batch = userSearchPort.findPage(query);
            result.addAll(batch);
            if (batch.size() < 100) {
                return result;
            }
            page++;
        }
    }

    /**
     * 문자열에 공백이 아닌 내용이 있는지 확인한다.
     */
    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

}
