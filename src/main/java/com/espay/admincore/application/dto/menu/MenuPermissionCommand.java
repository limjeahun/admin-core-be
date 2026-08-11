package com.espay.admincore.application.dto.menu;

/**
 * 권한에 부여할 단일 메뉴의 조회·편집 권한 명령.
 *
 * @param menuCode 권한을 부여할 메뉴 코드
 * @param canView 메뉴 조회 허용 여부
 * @param canEdit 메뉴 편집 허용 여부
 */
public record MenuPermissionCommand(String menuCode, boolean canView, boolean canEdit) {
    /**
     * 메뉴 코드와 조회·편집 여부로 명령을 생성한다.
     * @param menuCode 메뉴 고유 코드
     * @param canView 조회 권한 여부
     * @param canEdit 편집 권한 여부
     * @return 메뉴 권한 명령
     */
    public static MenuPermissionCommand of(String menuCode, boolean canView, boolean canEdit) {
        return new MenuPermissionCommand(menuCode, canView, canEdit);
    }
}
