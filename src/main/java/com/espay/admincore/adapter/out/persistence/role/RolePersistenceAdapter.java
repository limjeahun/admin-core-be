package com.espay.admincore.adapter.out.persistence.role;

import com.espay.admincore.adapter.out.persistence.role.mapper.RolePersistenceMapper;
import com.espay.admincore.adapter.out.persistence.role.repository.RoleJpaRepository;
import com.espay.admincore.adapter.out.persistence.role.repository.RoleQueryDslRepository;
import com.espay.admincore.application.dto.role.RoleQuery;
import com.espay.admincore.application.port.out.role.RolePersistencePort;
import com.espay.admincore.domain.model.role.AdminRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 권한 CRUD는 Spring Data JPA, 동적 목록 검색은 QueryDSL에 위임하는 영속성 어댑터.
 */
@Repository
@RequiredArgsConstructor
public class RolePersistenceAdapter implements RolePersistencePort {
    private final RoleJpaRepository         repository;
    private final RoleQueryDslRepository    queryRepository;
    private final RolePersistenceMapper     mapper;

    /**
     * 권한 도메인 모델을 엔티티로 변환해 저장하고 DB 반영 결과를 도메인 모델로 반환한다.
     *
     * @param role 생성하거나 변경할 권한
     * @return 저장 결과가 반영된 권한 도메인 모델
     */
    @Override
    public AdminRole save(AdminRole role) {
        return mapper.toDomain(repository.save(mapper.toEntity(role)));
    }

    /**
     * 문자열 권한 ID를 DB 식별자 타입으로 변환하여 단건 조회한다.
     *
     * @param roleId 조회할 권한 ID
     * @return 일치하는 권한 도메인 모델 또는 빈 값
     */
    @Override
    public Optional<AdminRole> findById(String roleId) {
        return repository.findById(Long.valueOf(roleId)).map(mapper::toDomain);
    }

    /**
     * 신규 생성과 이름 변경 시 중복 여부를 검사할 권한을 이름으로 조회한다.
     *
     * @param roleName 조회할 권한명
     * @return 일치하는 권한 도메인 모델 또는 빈 값
     */
    @Override
    public Optional<AdminRole> findByName(String roleName) {
        return repository.findByName(roleName).map(mapper::toDomain);
    }

    /**
     * 권한 검색 조건과 페이지 정보를 QueryDSL 저장소에 전달하여 현재 페이지를 조회한다.
     *
     * @param query 권한명·사용 여부·페이지 조건
     * @return 조건에 맞는 현재 페이지의 권한 목록
     */
    @Override
    public List<AdminRole> findPage(RoleQuery query) {
        return queryRepository.findPage(query).stream().map(mapper::toDomain).toList();
    }

    /**
     * 권한 목록과 동일한 검색 조건에 맞는 전체 건수를 계산한다.
     *
     * @param query 권한명과 사용 여부 검색 조건
     * @return 조건에 맞는 전체 권한 수
     */
    @Override
    public long count(RoleQuery query) {
        return queryRepository.count(query);
    }
}
