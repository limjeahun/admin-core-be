package com.espay.admincore.adapter.out.persistence.history;

import com.espay.admincore.adapter.out.persistence.history.mapper.LoginHistoryPersistenceMapper;
import com.espay.admincore.adapter.out.persistence.history.repository.LoginHistoryJpaRepository;
import com.espay.admincore.adapter.out.persistence.history.repository.LoginHistoryQueryDslRepository;
import com.espay.admincore.application.dto.history.FindLatestLoginReasonQuery;
import com.espay.admincore.application.dto.history.LoginHistoryQuery;
import com.espay.admincore.application.port.out.history.LoginHistoryPersistencePort;
import com.espay.admincore.domain.model.history.LoginHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 로그인 이력 저장은 Spring Data JPA, 동적 검색과 최근 사유 조회는 QueryDSL에 위임하는 어댑터.
 */
@Repository
@RequiredArgsConstructor
public class LoginHistoryPersistenceAdapter implements LoginHistoryPersistencePort {
    private final LoginHistoryJpaRepository         repository;
    private final LoginHistoryQueryDslRepository    queryRepository;
    private final LoginHistoryPersistenceMapper     mapper;

    /**
     * 도메인 이력을 엔티티로 변환해 호출자의 트랜잭션과 분리된 새 트랜잭션에 저장한다.
     * 인증 처리 이후 예외가 발생해도 이미 기록한 감사 이력이 함께 롤백되지 않도록 보장한다.
     *
     * @param history 저장할 로그인 이력
     * @return 저장된 로그인 이력
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public LoginHistory save(LoginHistory history) {
        return mapper.toDomain(repository.save(mapper.toEntity(history)));
    }
    /**
     * QueryDSL로 조건과 페이지에 맞는 로그인 이력을 조회한다.
     *
     * @param query 검색 조건
     * @return 현재 페이지 이력
     */
    @Override
    public List<LoginHistory> findPage(LoginHistoryQuery query) {
        return queryRepository.findPage(query);
    }
    /**
     * QueryDSL로 검색 조건에 맞는 전체 로그인 이력 수를 조회한다.
     *
     * @param query 검색 조건
     * @return 전체 이력 수
     */
    @Override
    public long count(LoginHistoryQuery query) {
        return queryRepository.count(query);
    }
    /**
     * OTP 이력에 연결할 가장 최근 비밀번호 로그인 사유를 조회한다.
     *
     * @param query 사용자, 입력 ID와 요청 IP를 묶은 조회 조건
     * @return 최근 접속 사유 또는 {@code null}
     */
    @Override
    public String findLatestLoginReason(FindLatestLoginReasonQuery query) {
        return queryRepository.findLatestLoginReason(query);
    }
}
