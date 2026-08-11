package com.espay.admincore.adapter.out.persistence.user.repository;

import com.espay.admincore.adapter.out.persistence.user.entity.UserJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 사용자 엔티티의 CRUD와 고유 필드·권한별 파생 조회를 제공하는 Spring Data JPA 저장소.
 */
public interface UserJpaRepository extends JpaRepository<UserJpaEntity, Long> {
    /**
     * 로그인 인증과 중복 판단에 사용할 사용자 엔티티를 로그인 ID로 조회한다.
     *
     * @param loginId 조회할 로그인 ID
     * @return 일치하는 사용자 엔티티 또는 빈 값
     */
    Optional<UserJpaEntity> findByLoginId(String loginId);

    /**
     * 지정한 이메일 주소를 사용하는 사용자 엔티티를 조회한다.
     *
     * @param email 조회할 이메일 주소
     * @return 일치하는 사용자 엔티티 또는 빈 값
     */
    Optional<UserJpaEntity> findByEmail(String email);

    /**
     * 동일 로그인 ID를 가진 사용자 엔티티가 존재하는지 확인한다.
     *
     * @param loginId 중복 여부를 검사할 로그인 ID
     * @return 동일 로그인 ID가 존재하면 {@code true}
     */
    boolean existsByLoginId(String loginId);

    /**
     * 지정한 권한에 연결된 사용자 엔티티 수를 계산한다.
     *
     * @param roleId 사용자 수를 계산할 권한 ID
     * @return 해당 권한을 사용 중인 사용자 수
     */
    long countByRoleId(Long roleId);
}
