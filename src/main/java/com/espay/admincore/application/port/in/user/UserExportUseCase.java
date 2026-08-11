package com.espay.admincore.application.port.in.user;

import com.espay.admincore.application.dto.file.ExcelDownloadResult;
import com.espay.admincore.application.dto.user.DownloadUsersExcelCommand;

/**
 * 사용자 검색 결과를 Excel 문서로 내보내는 유스케이스.
 */
public interface UserExportUseCase {
    /**
     * 조건에 맞는 사용자를 조회해 Excel을 생성하고 다운로드 감사 이력을 기록한다.
     *
     * @param command 검색 조건과 다운로드 감사 정보
     * @return 파일명과 Excel 바이너리
     */
    ExcelDownloadResult downloadUsersExcel(DownloadUsersExcelCommand command);
}
