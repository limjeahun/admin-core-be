package com.espay.admincore.application.port.out.history;

import com.espay.admincore.application.dto.history.FileHistoryQuery;
import com.espay.admincore.domain.model.file.FileHistory;

import java.util.List;

/**
 * 파일 업로드·다운로드 감사 이력의 저장과 검색을 담당하는 출력 포트.
 */
public interface FileHistoryPersistencePort {
    /**
     * 파일 처리 이력을 저장한다.
     *
     * @param history 저장할 파일 이력
     * @return 저장된 파일 이력
     */
    FileHistory save(FileHistory history);
    /**
     * 검색 및 페이지 조건에 맞는 파일 이력을 조회한다.
     *
     * @param query 파일 이력 검색 조건
     * @return 사용자·메뉴 표시 정보가 조합된 이력 목록
     */
    List<FileHistory> findPage(FileHistoryQuery query);
    /**
     * 검색 조건에 맞는 파일 이력 수를 조회한다.
     *
     * @param query 파일 이력 검색 조건
     * @return 전체 이력 수
     */
    long count(FileHistoryQuery query);
}
