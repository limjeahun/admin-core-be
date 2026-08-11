package com.espay.admincore.application.dto.role;

/**
 * 기존 관리자 권한의 기본 정보를 수정하는 명령.
 *
 * @param roleId 수정할 권한 ID
 * @param name 변경할 권한명
 * @param description 변경할 권한 설명
 * @param useYn 변경할 사용 여부(Y/N)
 */
public record UpdateRoleCommand(String roleId, String name, String description, String useYn) {
    /**
     * 대상 권한과 변경 값으로 수정 명령을 생성한다.
     * @param roleId 수정할 권한 ID
     * @param name 변경할 권한명
     * @param description 변경할 설명
     * @param useYn 변경할 사용 여부
     * @return 권한 수정 명령
     */
    public static UpdateRoleCommand of(String roleId, String name, String description, String useYn) {
        return new UpdateRoleCommand(roleId, name, description, useYn);
    }
}
