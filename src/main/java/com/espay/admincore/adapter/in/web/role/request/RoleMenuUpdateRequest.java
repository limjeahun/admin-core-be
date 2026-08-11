package com.espay.admincore.adapter.in.web.role.request;

import com.espay.admincore.application.dto.menu.MenuPermissionCommand;
import jakarta.validation.Valid;

import java.util.List;

/**
 * 특정 권한의 전체 메뉴 권한을 교체하는 HTTP 요청.
 *
 * @param permissions 새로 적용할 메뉴별 권한 목록
 */
public record RoleMenuUpdateRequest(List<@Valid RoleMenuPermissionRequest> permissions) {
    /**
     * HTTP 메뉴 권한 목록을 애플리케이션 메뉴 권한 명령 목록으로 변환한다.
     *
     * @return 애플리케이션 계층에 전달할 메뉴 권한 목록
     */
    public List<MenuPermissionCommand> toCommands() {
        return permissions == null ? List.of()
                : permissions.stream().map(RoleMenuPermissionRequest::toCommand).toList();
    }
}
