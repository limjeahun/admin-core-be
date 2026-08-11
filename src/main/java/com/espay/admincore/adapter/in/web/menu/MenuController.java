package com.espay.admincore.adapter.in.web.menu;

import com.espay.admincore.adapter.in.web.menu.response.MenuListResponse;
import com.espay.admincore.application.port.in.menu.MenuQueryUseCase;
import com.espay.admincore.common.api.ApiResponse;
import com.espay.admincore.adapter.in.web.resolver.CurrentUserId;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 현재 사용자 메뉴와 전체 활성 메뉴 조회 요청을 메뉴 유스케이스로 전달한다.
 */
@RestController
@RequestMapping("/api/v1/menus")
@RequiredArgsConstructor
public class MenuController implements MenuSpec {
    private final MenuQueryUseCase menuQueryUseCase;

    /**
     * 현재 인증된 사용자가 접근할 수 있는 메뉴 목록을 조회한다.
     *
     * @param userId 현재 인증된 사용자 ID
     * @return 접근 가능한 메뉴 목록이 포함된 HTTP 응답
     */
    @GetMapping("/me")
    @Override
    public ResponseEntity<ApiResponse<MenuListResponse>> getMyMenus(@CurrentUserId String userId) {
        return ApiResponse.ok(
                MenuListResponse.from(menuQueryUseCase.getMyMenus(userId))
        ).toResponseEntity();
    }

    /**
     * 활성 상태인 전체 메뉴 목록을 조회한다.
     *
     * @return 활성 메뉴 목록이 포함된 HTTP 응답
     */
    @GetMapping("/active")
    @Override
    public ResponseEntity<ApiResponse<MenuListResponse>> getActiveMenus() {
        return ApiResponse.ok(
                MenuListResponse.from(menuQueryUseCase.getActiveMenus())
        ).toResponseEntity();
    }
}
