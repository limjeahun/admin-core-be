package com.espay.admincore.domain.model.role;

import com.espay.admincore.domain.model.menu.MenuPermissionAction;

/**
 * 관리자 권한과 단일 메뉴 사이의 조회·편집 권한.
 *
 * @param roleId 권한 ID
 * @param menuCode 메뉴 코드
 * @param canView 조회 허용 여부
 * @param canEdit 편집 허용 여부
 */
public record RoleMenuPermission(String roleId, String menuCode, boolean canView, boolean canEdit) {

    /**
     * 편집 권한만 단독으로 부여되는 잘못된 상태를 생성 시점에 차단한다.
     *
     * @param roleId 권한 ID
     * @param menuCode 메뉴 코드
     * @param canView 조회 허용 여부
     * @param canEdit 편집 허용 여부
     * @throws IllegalArgumentException 편집은 허용하지만 조회는 허용하지 않은 경우
     */
    public RoleMenuPermission {
        if (canEdit && !canView) {
            throw new IllegalArgumentException("편집 권한은 조회 권한과 함께 설정해야 합니다.");
        }
    }

    /**
     * 현재 메뉴 권한이 요청 작업을 허용하는지 판단한다.
     *
     * @param action 확인할 조회 또는 편집 작업
     * @return 조회 권한이 있고 요청 작업이 조회이거나 편집 권한도 있으면 {@code true}
     */
    public boolean allows(MenuPermissionAction action) {
        return canView && (action == MenuPermissionAction.VIEW || canEdit);
    }
}
