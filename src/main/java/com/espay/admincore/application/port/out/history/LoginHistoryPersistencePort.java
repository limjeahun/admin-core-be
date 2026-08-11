package com.espay.admincore.application.port.out.history;

import com.espay.admincore.application.dto.history.FindLatestLoginReasonQuery;
import com.espay.admincore.application.dto.history.LoginHistoryQuery;
import com.espay.admincore.domain.model.history.LoginHistory;

import java.util.List;

/**
 * 로그인과 OTP 인증 이력의 저장 및 조회를 담당하는 출력 포트.
 */
public interface LoginHistoryPersistencePort {

    /**
     * 로그인 또는 OTP 인증 이력을 저장한다.
     *
     * @param history 저장할 인증 이력
     * @return 저장된 인증 이력
     */
    LoginHistory save(LoginHistory history);

    /**
     * 검색 및 페이지 조건에 맞는 인증 이력을 조회한다.
     *
     * @param query 인증 이력 검색 조건
     * @return 조회된 인증 이력 목록
     */
    List<LoginHistory> findPage(LoginHistoryQuery query);

    /**
     * 검색 조건에 맞는 인증 이력 수를 조회한다.
     *
     * @param query 인증 이력 검색 조건
     * @return 인증 이력 수
     */
    long count(LoginHistoryQuery query);

    /**
     * 사용자, 입력 ID 및 클라이언트 IP에 대응하는 가장 최근 로그인 사유를 조회한다.
     *
     * @param query 사용자, 입력 ID와 클라이언트 IP를 묶은 조회 조건
     * @return 가장 최근 로그인 사유, 조회 결과가 없으면 {@code null}
     */
    String findLatestLoginReason(FindLatestLoginReasonQuery query);
}
