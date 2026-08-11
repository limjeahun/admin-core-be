package com.espay.admincore.adapter.in.web.role.request;

import com.espay.admincore.application.dto.role.CreateRoleCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

/**
 * 신규 관리자 권한과 초기 메뉴 권한을 등록하는 HTTP 요청.
 *
 * @param name 필수 권한명
 * @param description 권한 설명
 * @param useYn 사용 여부(Y/N)
 * @param permissions 최초로 연결할 메뉴 권한 목록
 */
public record RoleCreateRequest(@NotBlank String name, String description, String useYn,
                                List<@Valid RoleMenuPermissionRequest> permissions) {
    /**
     * 누락된 메뉴 권한 목록을 빈 목록으로 보정해 생성 명령으로 변환한다.
     *
     * @return 권한 생성 명령
     */
    public CreateRoleCommand toCommand() {
        return CreateRoleCommand.of(name, description, useYn,
                permissions == null ? List.of() : permissions.stream().map(RoleMenuPermissionRequest::toCommand).toList());
    }
}
