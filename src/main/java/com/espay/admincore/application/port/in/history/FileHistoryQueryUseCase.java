package com.espay.admincore.application.port.in.history;

import com.espay.admincore.application.dto.history.FileHistoryListResult;
import com.espay.admincore.application.dto.history.FileHistoryQuery;

/**
 * 파일 업로드·다운로드 감사 이력을 검색하는 유스케이스.
 */
public interface FileHistoryQueryUseCase {
    /**
     * 필터와 페이지 조건에 맞는 파일 이력을 조회한다.
     *
     * @param query 기간, 처리 구분, 성공 여부와 검색 조건
     * @return 파일 이력 페이지
     */
    FileHistoryListResult getFileHistory(FileHistoryQuery query);
}
