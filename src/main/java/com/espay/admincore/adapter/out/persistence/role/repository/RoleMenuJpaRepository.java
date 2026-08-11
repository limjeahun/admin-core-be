package com.espay.admincore.adapter.out.persistence.role.repository;

import com.espay.admincore.adapter.out.persistence.role.entity.RoleMenuId;
import com.espay.admincore.adapter.out.persistence.role.entity.RoleMenuJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 역할별 메뉴 권한의 조회와 전체 교체에 필요한 Spring Data JPA 저장소.
 */
public interface RoleMenuJpaRepository extends JpaRepository<RoleMenuJpaEntity, RoleMenuId> {
    /**
     * 권한 ID에 연결된 메뉴 권한을 메뉴 코드 순으로 조회한다.
     *
     * @param roleId 권한 ID
     * @return 역할별 메뉴 권한 엔티티 목록
     */
    List<RoleMenuJpaEntity> findByIdRoleIdOrderByIdMenuCode(Long roleId);
    /**
     * 권한 ID에 연결된 기존 메뉴 권한을 모두 삭제한다.
     *
     * @param roleId 권한 ID
     */
    void deleteByIdRoleId(Long roleId);
}
