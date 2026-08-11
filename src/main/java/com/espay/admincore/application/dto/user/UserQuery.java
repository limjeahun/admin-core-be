package com.espay.admincore.application.dto.user;

/**
 * 관리자 사용자 목록을 검색하기 위한 필터와 페이지 조건.
 *
 * @param roleId 특정 권한의 사용자만 조회할 권한 ID
 * @param status 사용자 활성 상태 필터
 * @param conditionType 검색어를 적용할 필드 구분
 * @param keyword 로그인 ID, 사용자명, 이메일 등 검색어
 * @param page 0부터 시작하는 페이지 번호
 * @param size 1~100 범위로 보정되는 페이지 크기
 */
public record UserQuery(
        String roleId,
        String status,
        String conditionType,
        String keyword,
        int page,
        int size
) {
    /**
     * 페이지 번호를 0 이상, 페이지 크기를 1~100 범위로 정규화한다.
     * 사용자 필터와 검색어는 저장소의 동적 조건 생성 단계에서 해석할 수 있도록 그대로 유지한다.
     *
     * @param roleId 특정 권한의 사용자만 조회할 권한 ID
     * @param status 사용자 활성 상태 필터
     * @param conditionType 검색 필드 구분
     * @param keyword 검색어
     * @param page 정규화할 페이지 번호
     * @param size 정규화할 페이지 크기
     */
    public UserQuery {
        page = Math.max(0, page);
        size = Math.min(100, Math.max(1, size));
    }

    /**
     * 권한·상태·검색어·페이지 정보로 사용자 질의를 생성한다.
     * @param roleId 권한 ID 필터
     * @param status 사용자 상태 필터
     * @param conditionType 검색 필드 구분
     * @param keyword 검색어
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return 정규화된 사용자 목록 질의
     */
    public static UserQuery of(String roleId, String status, String conditionType,
                               String keyword, int page, int size) {
        return new UserQuery(roleId, status, conditionType, keyword, page, size);
    }
}
