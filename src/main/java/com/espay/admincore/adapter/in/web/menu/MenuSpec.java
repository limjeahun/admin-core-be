package com.espay.admincore.adapter.in.web.menu;

import com.espay.admincore.adapter.in.web.menu.response.MenuListResponse;
import com.espay.admincore.common.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

/**
 * 현재 사용자 메뉴 및 활성 메뉴 조회 API의 Swagger 명세.
 */
@Tag(name = "메뉴 API", description = "현재 사용자 메뉴 및 활성 메뉴 조회 API")
public interface MenuSpec {

    /**
     * 현재 인증 사용자가 접근 가능한 메뉴를 조회한다.
     *
     * @param userId 역할과 메뉴 권한을 조회할 인증 사용자 ID
     * @return 사용자에게 허용된 메뉴와 조회·편집 권한을 담은 HTTP 응답
     */
    @Operation(summary = "내 메뉴 조회", description = "현재 인증 사용자가 접근 가능한 메뉴 목록을 조회합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "내 메뉴 조회 성공",
            content = @Content(schema = @Schema(implementation = MenuListResponse.class))
    )
    ResponseEntity<ApiResponse<MenuListResponse>> getMyMenus(@Parameter(hidden = true) String userId);

    /**
     * 활성 상태인 전체 메뉴를 조회한다.
     *
     * @return 코드 카탈로그에 정의된 활성 메뉴 목록 HTTP 응답
     */
    @Operation(summary = "활성 메뉴 조회", description = "활성 상태의 전체 메뉴 목록을 조회합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "활성 메뉴 조회 성공",
            content = @Content(schema = @Schema(implementation = MenuListResponse.class))
    )
    ResponseEntity<ApiResponse<MenuListResponse>> getActiveMenus();
}
