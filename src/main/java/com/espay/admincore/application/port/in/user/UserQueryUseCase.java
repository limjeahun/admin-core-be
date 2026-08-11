package com.espay.admincore.application.port.in.user;

import com.espay.admincore.application.dto.user.UserListResult;
import com.espay.admincore.application.dto.user.UserQuery;
import com.espay.admincore.application.dto.user.UserResult;

/**
 * 관리자 사용자의 상세 또는 페이지 목록을 조회하는 유스케이스.
 */
public interface UserQueryUseCase {
    /**
     * 사용자 ID로 사용자와 부여된 권한 정보를 조회한다.
     *
     * @param userId 조회할 사용자 ID
     * @return 사용자 상세 결과
     */
    UserResult getUser(String userId);
    /**
     * 역할, 상태, 검색어와 페이지 조건에 맞는 사용자를 조회한다.
     *
     * @param query 사용자 검색 조건
     * @return 사용자 페이지 결과
     */
    UserListResult getUsers(UserQuery query);
}
