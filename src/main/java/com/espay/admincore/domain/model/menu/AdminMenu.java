package com.espay.admincore.domain.model.menu;

/**
 * 관리자 화면에서 접근 제어와 내비게이션 구성에 사용하는 메뉴 정의.
 *
 * @param menuCode 권한 저장과 조회에 사용하는 고유 메뉴 코드
 * @param name 화면에 표시할 메뉴명
 * @param path 프론트엔드 라우팅 경로, 그룹 메뉴는 {@code null}
 * @param parentMenuCode 상위 그룹 메뉴 코드, 최상위 메뉴는 {@code null}
 * @param sortOrder 동일 계층에서의 표시 순서
 */
public record AdminMenu(
        String menuCode,
        String name,
        String path,
        String parentMenuCode,
        int sortOrder
) {
}
