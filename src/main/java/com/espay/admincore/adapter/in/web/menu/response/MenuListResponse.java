package com.espay.admincore.adapter.in.web.menu.response;

import com.espay.admincore.application.dto.menu.MenuListResult;

import java.util.List;

/**
 * 관리자 메뉴 목록 HTTP 응답.
 *
 * @param items 계층, 경로와 현재 권한 상태가 포함된 메뉴 목록
 */
public record MenuListResponse(List<MenuResponse> items) {
    /**
     * 애플리케이션 메뉴 목록을 HTTP 응답 모델로 변환한다.
     *
     * @param result 메뉴 목록 결과
     * @return 외부 응답용 메뉴 목록
     */
    public static MenuListResponse from(MenuListResult result) {
        return new MenuListResponse(result.items().stream().map(MenuResponse::from).toList());
    }
}
