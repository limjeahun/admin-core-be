package com.espay.admincore.application.port.out.role;

import com.espay.admincore.application.dto.role.RoleQuery;
import com.espay.admincore.domain.model.role.AdminRole;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * 관리자 권한의 저장과 조회를 담당하는 출력 포트.
 */
public interface RolePersistencePort {

    /**
     * 권한 정보를 저장한다.
     *
     * @param role 저장할 권한
     * @return 저장된 권한
     */
    AdminRole save(AdminRole role);

    /**
     * 권한 ID로 권한을 조회한다.
     *
     * @param roleId 권한 ID
     * @return 조회된 권한 또는 빈 값
     */
    Optional<AdminRole> findById(String roleId);

    /**
     * 여러 권한 ID에 해당하는 권한을 한 번에 조회한다.
     *
     * @param roleIds 조회할 권한 ID 목록
     * @return 조회된 권한 목록
     */
    List<AdminRole> findByIds(Collection<String> roleIds);

    /**
     * 권한명으로 권한을 조회한다.
     *
     * @param roleName 권한명
     * @return 조회된 권한 또는 빈 값
     */
    Optional<AdminRole> findByName(String roleName);

    /**
     * 검색 및 페이지 조건에 맞는 권한 목록을 조회한다.
     *
     * @param query 권한 검색 조건
     * @return 조회된 권한 목록
     */
    List<AdminRole> findPage(RoleQuery query);

    /**
     * 검색 조건에 맞는 권한 수를 조회한다.
     *
     * @param query 권한 검색 조건
     * @return 권한 수
     */
    long count(RoleQuery query);
}
