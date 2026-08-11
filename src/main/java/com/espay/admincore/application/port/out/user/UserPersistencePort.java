package com.espay.admincore.application.port.out.user;

import com.espay.admincore.domain.model.user.AdminUser;

/**
 * 관리자 사용자 저장과 권한별 사용자 집계를 담당하는 출력 포트.
 */
public interface UserPersistencePort {
    /**
     * 신규 또는 변경된 사용자를 저장한다.
     *
     * @param user 저장할 사용자
     * @return 저장된 사용자
     */
    AdminUser save(AdminUser user);
    /**
     * 특정 권한이 부여된 사용자 수를 계산한다.
     *
     * @param roleId 권한 ID
     * @return 권한별 사용자 수
     */
    long countByRoleId(String roleId);
}
