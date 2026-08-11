package com.espay.admincore.adapter.in.web.role.request;

import com.espay.admincore.application.dto.role.UpdateRoleCommand;

/**
 * 관리자 권한의 기본 정보를 수정하는 HTTP 요청.
 *
 * @param name 변경할 권한명
 * @param description 변경할 권한 설명
 * @param useYn 변경할 사용 여부(Y/N)
 */
public record RoleUpdateRequest(String name, String description, String useYn) {
    /**
     * URL의 권한 ID를 요청값과 결합해 수정 명령으로 변환한다.
     *
     * @param roleId 수정할 권한 ID
     * @return 권한 수정 명령
     */
    public UpdateRoleCommand toCommand(String roleId) {
        return UpdateRoleCommand.of(roleId, name, description, useYn);
    }
}
