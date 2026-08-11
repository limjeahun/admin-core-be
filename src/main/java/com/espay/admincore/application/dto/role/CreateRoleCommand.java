package com.espay.admincore.application.dto.role;

import com.espay.admincore.application.dto.menu.MenuPermissionCommand;

import java.util.List;

/**
 * 신규 관리자 권한과 초기 메뉴 권한을 생성하는 명령.
 *
 * @param name 중복될 수 없는 권한명
 * @param description 권한 용도 설명
 * @param useYn 사용 여부(Y/N)
 * @param permissions 최초로 연결할 메뉴별 권한 목록
 */
public record CreateRoleCommand(String name, String description, String useYn,
                                List<MenuPermissionCommand> permissions) {
    /**
     * 권한 기본 정보와 메뉴 권한으로 생성 명령을 만든다.
     * @param name 권한명
     * @param description 권한 설명
     * @param useYn 사용 여부
     * @param permissions 초기 메뉴 권한 목록
     * @return 권한 생성 명령
     */
    public static CreateRoleCommand of(String name, String description, String useYn,
                                       List<MenuPermissionCommand> permissions) {
        return new CreateRoleCommand(name, description, useYn, permissions);
    }
}
