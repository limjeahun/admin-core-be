package com.espay.admincore.application.port.in.menu;

import com.espay.admincore.application.dto.menu.MenuListResult;
import com.espay.admincore.application.dto.menu.MenuPermissionCommand;

import java.util.List;

/**
 * 권한별 메뉴 연결과 조회·편집 권한을 관리하는 유스케이스.
 */
public interface RoleMenuUseCase {
    /**
     * 전체 메뉴 카탈로그에 권한의 현재 연결 상태를 조합해 조회한다.
     *
     * @param roleId 조회할 권한 ID
     * @return 메뉴별 연결·조회·편집 상태
     */
    MenuListResult getRoleMenus(String roleId);
    /**
     * 권한의 기존 메뉴 연결을 요청 목록으로 전체 교체한다.
     *
     * @param roleId 변경할 권한 ID
     * @param permissions 새로 적용할 메뉴별 권한 목록
     */
    void updateRoleMenus(String roleId, List<MenuPermissionCommand> permissions);
}
