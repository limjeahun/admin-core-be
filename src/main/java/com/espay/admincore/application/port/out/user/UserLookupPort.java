package com.espay.admincore.application.port.out.user;

import com.espay.admincore.domain.model.user.AdminUser;

import java.util.Optional;

/**
 * 사용자 식별 정보로 관리자 사용자를 조회하는 출력 포트.
 */
public interface UserLookupPort {

    /**
     * 사용자 ID로 관리자를 조회한다.
     *
     * @param userId 사용자 ID
     * @return 조회된 관리자 또는 빈 값
     */
    Optional<AdminUser> findById(String userId);

    /**
     * 로그인 ID로 관리자를 조회한다.
     *
     * @param loginId 로그인 ID
     * @return 조회된 관리자 또는 빈 값
     */
    Optional<AdminUser> findByLoginId(String loginId);

    /**
     * 이메일로 관리자를 조회한다.
     *
     * @param email 이메일 주소
     * @return 조회된 관리자 또는 빈 값
     */
    Optional<AdminUser> findByEmail(String email);

    /**
     * 로그인 ID를 사용하는 관리자가 존재하는지 확인한다.
     *
     * @param loginId 로그인 ID
     * @return 사용자가 존재하면 {@code true}
     */
    boolean existsByLoginId(String loginId);
}
