package com.espay.admincore.adapter.in.web.role.request;

import com.espay.admincore.application.dto.menu.MenuPermissionCommand;
import jakarta.validation.constraints.NotBlank;

/**
 * 권한에 연결할 단일 메뉴의 작업 권한 HTTP 입력.
 *
 * @param menuCode 필수 메뉴 코드
 * @param canView 조회 허용 여부
 * @param canEdit 편집 허용 여부
 */
public record RoleMenuPermissionRequest(@NotBlank String menuCode, boolean canView, boolean canEdit) {
    /**
     * HTTP 입력을 애플리케이션 메뉴 권한 명령으로 변환한다.
     *
     * @return 메뉴별 조회·편집 명령
     */
    public MenuPermissionCommand toCommand() {
        return MenuPermissionCommand.of(menuCode, canView, canEdit);
    }
}
