package com.espay.admincore.adapter.out.persistence.history;

import com.espay.admincore.adapter.out.persistence.history.mapper.FileHistoryPersistenceMapper;
import com.espay.admincore.adapter.out.persistence.history.repository.FileHistoryJpaRepository;
import com.espay.admincore.adapter.out.persistence.history.repository.FileHistoryQueryDslRepository;
import com.espay.admincore.application.dto.history.FileHistoryQuery;
import com.espay.admincore.application.port.out.history.FileHistoryPersistencePort;
import com.espay.admincore.domain.model.file.FileHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 파일 이력 저장은 Spring Data JPA, 동적 검색은 QueryDSL에 위임하는 영속성 어댑터.
 */
@Repository
@RequiredArgsConstructor
public class FileHistoryPersistenceAdapter implements FileHistoryPersistencePort {
    private final FileHistoryJpaRepository repository;
    private final FileHistoryQueryDslRepository queryRepository;
    private final FileHistoryPersistenceMapper mapper;

    /**
     * 도메인 이력을 엔티티로 변환해 호출자의 트랜잭션과 분리된 새 트랜잭션에 저장한다.
     * 읽기 전용 Excel 처리와 별개로 다운로드 성공·실패 감사 이력을 커밋한다.
     *
     * @param history 저장할 파일 이력
     * @return 저장된 파일 이력
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public FileHistory save(FileHistory history) {
        return mapper.toDomain(repository.save(mapper.toEntity(history)));
    }
    /**
     * QueryDSL로 조건과 페이지에 맞는 파일 이력을 조회한다.
     *
     * @param query 검색 조건
     * @return 현재 페이지 이력
     */
    @Override
    public List<FileHistory> findPage(FileHistoryQuery query) {
        return queryRepository.findPage(query);
    }
    /**
     * QueryDSL로 검색 조건에 맞는 전체 이력 수를 조회한다.
     *
     * @param query 검색 조건
     * @return 전체 이력 수
     */
    @Override
    public long count(FileHistoryQuery query) {
        return queryRepository.count(query);
    }
}
