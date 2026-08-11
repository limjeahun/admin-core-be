package com.espay.admincore.adapter.in.web.role;

import com.espay.admincore.adapter.in.web.menu.response.MenuListResponse;
import com.espay.admincore.adapter.in.web.role.request.RoleCreateRequest;
import com.espay.admincore.adapter.in.web.role.request.RoleMenuUpdateRequest;
import com.espay.admincore.adapter.in.web.role.request.RoleUpdateRequest;
import com.espay.admincore.adapter.in.web.role.response.RoleListResponse;
import com.espay.admincore.adapter.in.web.role.response.RoleResponse;
import com.espay.admincore.adapter.in.web.user.response.UserListResponse;
import com.espay.admincore.common.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

/**
 * 권한과 권한별 메뉴 및 사용자 관리 API의 Swagger 명세.
 */
@Tag(name = "권한 관리 API", description = "권한, 권한별 메뉴, 권한별 사용자 조회/수정 API")
public interface RoleSpec {

    /**
     * 검색 키워드와 페이지 조건으로 권한 목록을 조회한다.
     *
     * @param keyword 권한명에 적용할 선택 검색어
     * @param page 0부터 시작하는 페이지 번호
     * @param size 한 페이지에 조회할 권한 수
     * @return 현재 페이지의 권한 목록과 전체 건수를 담은 HTTP 응답
     */
    @Operation(summary = "권한 목록 조회", description = "검색 키워드와 페이지 조건으로 권한 목록을 조회합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "권한 목록 조회 성공",
            content = @Content(schema = @Schema(implementation = RoleListResponse.class))
    )
    ResponseEntity<ApiResponse<RoleListResponse>> getRoles(
            @Parameter(description = "검색 키워드") String keyword,
            @Parameter(description = "페이지 번호", example = "0") int page,
            @Parameter(description = "페이지 크기", example = "10") int size
    );

    /**
     * 권한 ID로 권한 상세 정보를 조회한다.
     *
     * @param roleId 조회할 권한 ID
     * @return 권한 상세 정보를 담은 HTTP 응답
     */
    @Operation(summary = "권한 단건 조회", description = "권한 ID로 권한 상세 정보를 조회합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "권한 단건 조회 성공",
            content = @Content(schema = @Schema(implementation = RoleResponse.class))
    )
    ResponseEntity<ApiResponse<RoleResponse>> getRole(
            @Parameter(description = "권한 ID", required = true) String roleId
    );

    /**
     * 신규 권한을 등록한다.
     *
     * @param request 권한명·설명·사용 여부를 담은 생성 요청
     * @return 생성된 권한 상세를 담은 HTTP 201 응답
     */
    @Operation(summary = "권한 등록", description = "신규 권한을 등록합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "권한 등록 성공",
            content = @Content(schema = @Schema(implementation = RoleResponse.class))
    )
    ResponseEntity<ApiResponse<RoleResponse>> createRole(RoleCreateRequest request);

    /**
     * 권한 ID를 기준으로 권한 정보를 수정한다.
     *
     * @param roleId 수정할 권한 ID
     * @param request 변경할 권한명·설명·사용 여부
     * @return 변경된 권한 상세를 담은 HTTP 응답
     */
    @Operation(summary = "권한 수정", description = "권한 ID 기준으로 권한 정보를 수정합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "권한 수정 성공",
            content = @Content(schema = @Schema(implementation = RoleResponse.class))
    )
    ResponseEntity<ApiResponse<RoleResponse>> updateRole(
            @Parameter(description = "권한 ID", required = true) String roleId,
            RoleUpdateRequest request
    );

    /**
     * 권한에 연결된 메뉴와 권한 정보를 조회한다.
     *
     * @param roleId 메뉴 권한을 조회할 권한 ID
     * @return 역할에 연결된 메뉴별 조회·편집 권한 HTTP 응답
     */
    @Operation(summary = "권한 메뉴 조회", description = "권한 ID 기준으로 연결된 메뉴 목록을 조회합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "권한 메뉴 조회 성공",
            content = @Content(schema = @Schema(implementation = MenuListResponse.class))
    )
    ResponseEntity<ApiResponse<MenuListResponse>> getRoleMenus(
            @Parameter(description = "권한 ID", required = true) String roleId
    );

    /**
     * 권한에 연결된 메뉴와 권한 정보를 수정한다.
     *
     * @param roleId 메뉴 권한을 교체할 권한 ID
     * @param request 저장할 메뉴별 조회·편집 권한 목록
     * @return 메뉴 권한 변경 완료를 나타내는 HTTP 응답
     */
    @Operation(summary = "권한 메뉴 수정", description = "권한 ID 기준으로 연결 메뉴를 수정합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "권한 메뉴 수정 성공"
    )
    ResponseEntity<ApiResponse<Void>> updateRoleMenus(
            @Parameter(description = "권한 ID", required = true) String roleId,
            RoleMenuUpdateRequest request
    );

    /**
     * 권한에 연결된 사용자 목록을 조회한다.
     *
     * @param roleId 사용자를 조회할 권한 ID
     * @param page 0부터 시작하는 페이지 번호
     * @param size 한 페이지에 조회할 사용자 수
     * @return 해당 권한 사용자의 현재 페이지와 전체 건수를 담은 HTTP 응답
     */
    @Operation(summary = "권한 사용자 조회", description = "권한 ID 기준으로 연결된 사용자 목록을 조회합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "권한 사용자 조회 성공",
            content = @Content(schema = @Schema(implementation = UserListResponse.class))
    )
    ResponseEntity<ApiResponse<UserListResponse>> getRoleUsers(
            @Parameter(description = "권한 ID", required = true) String roleId,
            @Parameter(description = "페이지 번호", example = "0") int page,
            @Parameter(description = "페이지 크기", example = "10") int size
    );
}
