package com.espay.admincore.application.port.in.history;

import com.espay.admincore.application.dto.history.DownloadHistoryExcelCommand;
import com.espay.admincore.application.dto.history.ExcelDownloadResult;
import com.espay.admincore.application.dto.history.FileHistoryQuery;
import com.espay.admincore.application.dto.history.LoginHistoryQuery;

/**
 * 로그인 또는 파일 감사 이력을 Excel 문서로 내보내는 유스케이스.
 */
public interface HistoryExportUseCase {
    /**
     * 로그인·OTP 인증 이력을 조회해 Excel 문서를 생성하고 다운로드 이력을 기록한다.
     *
     * @param command 검색 조건과 다운로드 감사 정보
     * @return 파일명과 Excel 바이너리
     */
    ExcelDownloadResult downloadLoginHistoryExcel(DownloadHistoryExcelCommand<LoginHistoryQuery> command);
    /**
     * 파일 처리 이력을 조회해 Excel 문서를 생성하고 다운로드 이력을 기록한다.
     *
     * @param command 검색 조건과 다운로드 감사 정보
     * @return 파일명과 Excel 바이너리
     */
    ExcelDownloadResult downloadFileHistoryExcel(DownloadHistoryExcelCommand<FileHistoryQuery> command);
}
