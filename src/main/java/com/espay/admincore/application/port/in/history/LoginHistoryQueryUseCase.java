package com.espay.admincore.application.port.in.history;

import com.espay.admincore.application.dto.history.LoginHistoryListResult;
import com.espay.admincore.application.dto.history.LoginHistoryQuery;

/**
 * 비밀번호 로그인과 OTP 인증 감사 이력을 검색하는 유스케이스.
 */
public interface LoginHistoryQueryUseCase {
    /**
     * 필터와 페이지 조건에 맞는 로그인·OTP 이력을 조회한다.
     *
     * @param query 기간, 인증 단계, 성공 여부와 검색 조건
     * @return 로그인 인증 이력 페이지
     */
    LoginHistoryListResult getLoginHistory(LoginHistoryQuery query);
}
