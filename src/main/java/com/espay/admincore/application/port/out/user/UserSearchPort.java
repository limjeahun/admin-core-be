package com.espay.admincore.application.port.out.user;

import com.espay.admincore.application.dto.user.UserQuery;
import com.espay.admincore.domain.model.user.AdminUser;

import java.util.List;

/**
 * 동적 검색 조건과 페이지를 이용한 관리자 사용자 조회 출력 포트.
 */
public interface UserSearchPort {
    /**
     * 검색 조건에 맞는 현재 페이지 사용자를 조회한다.
     *
     * @param query 사용자 검색 조건
     * @return 사용자 도메인 모델 목록
     */
    List<AdminUser> findPage(UserQuery query);
    /**
     * 검색 조건에 맞는 전체 사용자 수를 조회한다.
     *
     * @param query 사용자 검색 조건
     * @return 전체 사용자 수
     */
    long count(UserQuery query);
}
