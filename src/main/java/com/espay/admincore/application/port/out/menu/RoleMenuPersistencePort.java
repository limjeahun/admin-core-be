package com.espay.admincore.application.port.out.menu;

import com.espay.admincore.domain.model.role.RoleMenuPermission;

import java.util.List;

/**
 * 권한과 메뉴 사이의 조회·편집 권한을 영속화하는 출력 포트.
 */
public interface RoleMenuPersistencePort {
    /**
     * 권한에 연결된 모든 메뉴 권한을 조회한다.
     *
     * @param roleId 권한 ID
     * @return 메뉴별 조회·편집 권한 목록
     */
    List<RoleMenuPermission> findByRoleId(String roleId);
    /**
     * 권한의 기존 메뉴 연결을 모두 삭제하고 새 목록으로 교체한다.
     *
     * @param roleId 변경할 권한 ID
     * @param permissions 새로 저장할 메뉴 권한 목록
     */
    void replaceRoleMenus(String roleId, List<RoleMenuPermission> permissions);
}
