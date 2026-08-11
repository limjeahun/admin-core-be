package com.espay.admincore.application.port.in.role;

import com.espay.admincore.application.dto.role.RoleListResult;
import com.espay.admincore.application.dto.role.RoleQuery;
import com.espay.admincore.application.dto.role.RoleResult;

/**
 * 관리자 권한의 상세 또는 페이지 목록을 조회하는 유스케이스.
 */
public interface RoleQueryUseCase {
    /**
     * 권한 ID로 상세 정보와 사용자 수를 조회한다.
     *
     * @param roleId 조회할 권한 ID
     * @return 권한 상세 결과
     */
    RoleResult getRole(String roleId);
    /**
     * 검색어와 페이지 조건에 맞는 권한 목록을 조회한다.
     *
     * @param query 권한 검색 조건
     * @return 권한 페이지 결과
     */
    RoleListResult getRoles(RoleQuery query);
}
