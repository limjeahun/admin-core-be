package com.espay.admincore.application.port.in.menu;

import com.espay.admincore.application.dto.menu.CheckMenuPermissionQuery;

/**
 * 현재 사용자와 권한 상태를 기준으로 메뉴 작업 허용 여부를 판단하는 유스케이스.
 */
public interface CheckMenuPermissionUseCase {
    /**
     * 요청 메뉴 중 하나에서 지정 작업 권한을 보유했는지 확인한다.
     *
     * @param query 사용자 ID, 후보 메뉴 코드와 작업 종류
     * @return 활성 사용자와 권한이 작업을 허용하면 {@code true}
     */
    boolean isAllowed(CheckMenuPermissionQuery query);
}
