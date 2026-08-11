package com.espay.admincore.application.port.in.role;

import com.espay.admincore.application.dto.role.CreateRoleCommand;
import com.espay.admincore.application.dto.role.RoleResult;
import com.espay.admincore.application.dto.role.UpdateRoleCommand;

/**
 * 관리자 권한을 생성하거나 기본 정보를 변경하는 명령 유스케이스.
 */
public interface RoleCommandUseCase {
    /**
     * 중복과 메뉴 권한을 검증해 신규 권한을 생성한다.
     *
     * @param command 권한 기본 정보와 초기 메뉴 권한
     * @return 생성된 권한과 사용자 수
     */
    RoleResult createRole(CreateRoleCommand command);
    /**
     * 기존 권한의 이름, 설명과 활성 상태를 변경한다.
     *
     * @param command 대상 권한 ID와 변경 정보
     * @return 수정된 권한과 사용자 수
     */
    RoleResult updateRole(UpdateRoleCommand command);
}
