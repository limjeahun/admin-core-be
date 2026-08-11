package com.espay.admincore.application.dto.menu;

import com.espay.admincore.domain.model.menu.MenuPermissionAction;

import java.util.Set;

/**
 * 사용자가 하나 이상의 메뉴에서 특정 작업을 수행할 수 있는지 확인하는 질의.
 *
 * @param userId 권한을 확인할 사용자 ID
 * @param menuCodes 하나라도 권한을 보유하면 허용할 메뉴 코드 집합
 * @param action 조회 또는 편집 권한 구분
 */
public record CheckMenuPermissionQuery(String userId, Set<String> menuCodes, MenuPermissionAction action) {
    /**
     * 사용자와 후보 메뉴·작업으로 권한 질의를 생성한다.
     * @param userId 인증 사용자 ID
     * @param menuCodes 허용 후보 메뉴 코드
     * @param action 검사할 조회 또는 편집 작업
     * @return 메뉴 권한 질의
     */
    public static CheckMenuPermissionQuery of(String userId, Set<String> menuCodes,
                                              MenuPermissionAction action) {
        return new CheckMenuPermissionQuery(userId, menuCodes, action);
    }
}
