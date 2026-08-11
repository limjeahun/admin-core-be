package com.espay.admincore.adapter.out.persistence.role.repository;

import com.espay.admincore.adapter.out.persistence.role.entity.RoleJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 권한 엔티티의 기본 CRUD와 권한명 조회를 제공하는 Spring Data JPA 저장소.
 */
public interface RoleJpaRepository extends JpaRepository<RoleJpaEntity, Long> {
    /**
     * 중복 검사에 사용할 정확한 권한명 조회를 수행한다.
     *
     * @param name 권한명
     * @return 일치하는 권한 엔티티 또는 빈 값
     */
    Optional<RoleJpaEntity> findByName(String name);
}
