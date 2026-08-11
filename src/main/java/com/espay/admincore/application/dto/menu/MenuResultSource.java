package com.espay.admincore.application.dto.menu;

import com.espay.admincore.domain.model.menu.AdminMenu;

/**
 * 메뉴 도메인 모델을 권한 상태가 포함된 조회 결과로 변환하기 위한 원본 데이터.
 *
 * @param menu 변환할 메뉴 정의
 * @param assigned 현재 권한의 메뉴 연결 여부
 * @param canView 조회 허용 여부
 * @param canEdit 편집 허용 여부
 */
public record MenuResultSource(AdminMenu menu, boolean assigned, boolean canView, boolean canEdit) {
    /**
     * 메뉴와 연결·권한 상태로 변환 원본을 생성한다.
     * @param menu 변환할 메뉴 정의
     * @param assigned 현재 권한의 메뉴 연결 여부
     * @param canView 조회 허용 여부
     * @param canEdit 편집 허용 여부
     * @return 메뉴 결과 변환 원본
     */
    public static MenuResultSource of(AdminMenu menu, boolean assigned, boolean canView, boolean canEdit) {
        return new MenuResultSource(menu, assigned, canView, canEdit);
    }
}
