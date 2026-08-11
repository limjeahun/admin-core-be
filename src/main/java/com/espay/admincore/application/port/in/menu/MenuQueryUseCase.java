package com.espay.admincore.application.port.in.menu;

import com.espay.admincore.application.dto.menu.MenuListResult;

/**
 * 사용자별 접근 메뉴와 전체 활성 메뉴 카탈로그를 조회하는 유스케이스.
 */
public interface MenuQueryUseCase {
    /**
     * 사용자의 권한에 연결된 조회 가능 메뉴와 상위 메뉴를 조회한다.
     *
     * @param userId 메뉴를 조회할 사용자 ID
     * @return 사용자에게 노출할 메뉴 목록
     */
    MenuListResult getMyMenus(String userId);
    /**
     * 권한 연결 여부와 무관하게 코드에 정의된 전체 메뉴를 조회한다.
     *
     * @return 전체 관리자 메뉴 목록
     */
    MenuListResult getActiveMenus();
}
