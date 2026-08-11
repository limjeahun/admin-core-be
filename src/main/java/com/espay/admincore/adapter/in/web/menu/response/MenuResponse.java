package com.espay.admincore.adapter.in.web.menu.response;

import com.espay.admincore.application.dto.menu.MenuResult;

/**
 * 단일 관리자 메뉴와 현재 권한 상태의 HTTP 응답.
 *
 * @param menuCode 메뉴 코드
 * @param name 표시명
 * @param path 프론트엔드 경로
 * @param parentMenuCode 상위 메뉴 코드
 * @param sortOrder 표시 순서
 * @param assigned 권한 연결 여부
 * @param canView 조회 허용 여부
 * @param canEdit 편집 허용 여부
 */
public record MenuResponse(String menuCode, String name, String path, String parentMenuCode, int sortOrder,
                           boolean assigned, boolean canView, boolean canEdit) {
    /**
     * 애플리케이션 메뉴 결과를 HTTP 응답으로 변환한다.
     *
     * @param result 변환할 메뉴 결과
     * @return 외부 응답용 메뉴
     */
    public static MenuResponse from(MenuResult result) {
        return new MenuResponse(result.menuCode(), result.name(), result.path(), result.parentMenuCode(),
                result.sortOrder(), result.assigned(), result.canView(), result.canEdit());
    }
}
