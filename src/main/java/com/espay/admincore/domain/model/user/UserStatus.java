package com.espay.admincore.domain.model.user;

/**
 * 관리자 계정의 로그인 및 API 사용 가능 상태.
 */
public enum UserStatus {
    /** 로그인과 권한 검사를 수행할 수 있는 활성 상태. */
    ACTIVE,
    /** 로그인과 보호된 API 사용이 차단된 비활성 상태. */
    INACTIVE;

    /**
     * API와 DB에서 사용하는 Y/N 또는 상태명을 도메인 상태로 변환한다.
     *
     * @param value Y, N, ACTIVE 또는 INACTIVE 표현값
     * @return 변환된 사용자 상태, 값이 없으면 활성 상태
     * @throws IllegalArgumentException 지원하지 않는 표현값인 경우
     */
    public static UserStatus from(String value) {
        if (value == null || value.isBlank() || "Y".equalsIgnoreCase(value) || "ACTIVE".equalsIgnoreCase(value)) {
            return ACTIVE;
        }
        if ("N".equalsIgnoreCase(value) || "INACTIVE".equalsIgnoreCase(value)) {
            return INACTIVE;
        }
        throw new IllegalArgumentException("지원하지 않는 사용자 상태입니다: " + value);
    }

    /**
     * 도메인 상태를 DB 저장 형식인 Y 또는 N으로 변환한다.
     *
     * @return 활성 상태면 Y, 비활성 상태면 N
     */
    public String toYn() {
        return this == ACTIVE ? "Y" : "N";
    }
}
