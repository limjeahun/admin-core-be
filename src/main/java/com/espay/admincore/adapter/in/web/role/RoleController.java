package com.espay.admincore.adapter.in.web.role;

import com.espay.admincore.adapter.in.web.menu.response.MenuListResponse;
import com.espay.admincore.adapter.in.web.role.request.*;
import com.espay.admincore.adapter.in.web.role.response.RoleListResponse;
import com.espay.admincore.adapter.in.web.role.response.RoleResponse;
import com.espay.admincore.adapter.in.web.user.response.UserListResponse;
import com.espay.admincore.application.dto.role.RoleQuery;
import com.espay.admincore.application.dto.user.UserQuery;
import com.espay.admincore.application.port.in.menu.RoleMenuUseCase;
import com.espay.admincore.application.port.in.role.RoleCommandUseCase;
import com.espay.admincore.application.port.in.role.RoleQueryUseCase;
import com.espay.admincore.application.port.in.user.UserQueryUseCase;
import com.espay.admincore.common.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 권한 조회와 변경, 권한별 메뉴 및 사용자 조회 요청을 처리한다.
 */
@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
public class RoleController implements RoleSpec {
    private final RoleCommandUseCase    commandUseCase;
    private final RoleQueryUseCase      queryUseCase;
    private final RoleMenuUseCase       roleMenuUseCase;
    private final UserQueryUseCase      userQueryUseCase;

    /**
     * 검색 키워드와 페이지 조건으로 권한 목록을 조회한다.
     *
     * @param keyword 권한 검색 키워드
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return 권한 목록이 포함된 HTTP 응답
     */
    @GetMapping
    @Override
    public ResponseEntity<ApiResponse<RoleListResponse>> getRoles(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.ok(
                RoleListResponse.from(
                        queryUseCase.getRoles(RoleQuery.of(keyword, page, size))
                )
        ).toResponseEntity();
    }

    /**
     * 권한 ID로 권한 상세 정보를 조회한다.
     *
     * @param roleId 조회할 권한 ID
     * @return 권한 상세 정보가 포함된 HTTP 응답
     */
    @GetMapping("/{roleId}")
    @Override
    public ResponseEntity<ApiResponse<RoleResponse>> getRole(@PathVariable String roleId) {
        return ApiResponse.ok(
                RoleResponse.from(queryUseCase.getRole(roleId))
        ).toResponseEntity();
    }

    /**
     * 신규 권한을 등록한다.
     *
     * @param request 권한 등록 정보
     * @return 등록된 권한 정보가 포함된 HTTP 응답
     */
    @PostMapping
    @Override
    public ResponseEntity<ApiResponse<RoleResponse>> createRole(@Valid @RequestBody RoleCreateRequest request) {
        return ApiResponse.created(
                RoleResponse.from(commandUseCase.createRole(request.toCommand()))
        ).toResponseEntity();
    }

    /**
     * 권한 ID를 기준으로 권한 정보를 수정한다.
     *
     * @param roleId 수정할 권한 ID
     * @param request 권한 수정 정보
     * @return 수정된 권한 정보가 포함된 HTTP 응답
     */
    @PutMapping("/{roleId}")
    @Override
    public ResponseEntity<ApiResponse<RoleResponse>> updateRole(@PathVariable String roleId,
                                                                 @Valid @RequestBody RoleUpdateRequest request) {
        return ApiResponse.ok(
                RoleResponse.from(
                        commandUseCase.updateRole(request.toCommand(roleId))
                )
        ).toResponseEntity();
    }

    /**
     * 권한에 연결된 메뉴와 메뉴별 권한을 조회한다.
     *
     * @param roleId 조회할 권한 ID
     * @return 메뉴 목록이 포함된 HTTP 응답
     */
    @GetMapping("/{roleId}/menus")
    @Override
    public ResponseEntity<ApiResponse<MenuListResponse>> getRoleMenus(@PathVariable String roleId) {
        return ApiResponse.ok(
                MenuListResponse.from(roleMenuUseCase.getRoleMenus(roleId))
        ).toResponseEntity();
    }

    /**
     * 권한에 연결할 메뉴와 메뉴별 권한을 수정한다.
     *
     * @param roleId 수정할 권한 ID
     * @param request 메뉴 권한 수정 정보
     * @return 처리 결과 HTTP 응답
     */
    @PutMapping("/{roleId}/menus")
    @Override
    public ResponseEntity<ApiResponse<Void>> updateRoleMenus(
            @PathVariable String roleId,
            @Valid @RequestBody RoleMenuUpdateRequest request
    ) {
        roleMenuUseCase.updateRoleMenus(roleId, request.toCommands());
        return ApiResponse.ok().toResponseEntity();
    }

    /**
     * 권한에 연결된 사용자 목록을 페이지 단위로 조회한다.
     *
     * @param roleId 조회할 권한 ID
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return 사용자 목록이 포함된 HTTP 응답
     */
    @GetMapping("/{roleId}/users")
    @Override
    public ResponseEntity<ApiResponse<UserListResponse>> getRoleUsers(
            @PathVariable String roleId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        queryUseCase.getRole(roleId);
        return ApiResponse.ok(
                UserListResponse.from(
                        userQueryUseCase.getUsers(
                                UserQuery.of(roleId, null, null, null, page, size)
                        )
                )
        ).toResponseEntity();
    }
}
