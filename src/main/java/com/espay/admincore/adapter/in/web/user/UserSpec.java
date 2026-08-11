package com.espay.admincore.adapter.in.web.user;

import com.espay.admincore.adapter.in.web.user.request.UserCreateRequest;
import com.espay.admincore.adapter.in.web.user.request.UserPasswordResetRequest;
import com.espay.admincore.adapter.in.web.user.request.UserSearchRequest;
import com.espay.admincore.adapter.in.web.user.request.UserUpdateRequest;
import com.espay.admincore.adapter.in.web.user.response.UserListResponse;
import com.espay.admincore.adapter.in.web.user.response.UserResponse;
import com.espay.admincore.common.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

/**
 * 사용자 조회, 등록, 수정 및 Excel 다운로드 API의 Swagger 명세.
 */
@Tag(name = "사용자 관리 API", description = "사용자 조회, 등록, 수정, 비밀번호 초기화, Excel 다운로드 API")
public interface UserSpec {

    /**
     * 검색 조건과 페이지 조건으로 사용자 목록을 조회한다.
     *
     * @param request 권한·상태·선택 검색어·페이지 조건
     * @return 현재 페이지의 사용자 목록과 전체 건수를 담은 HTTP 응답
     */
    @Operation(summary = "사용자 목록 조회", description = "검색 조건과 페이지 조건으로 사용자 목록을 조회합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "사용자 목록 조회 성공",
            content = @Content(schema = @Schema(implementation = UserListResponse.class))
    )
    ResponseEntity<ApiResponse<UserListResponse>> getUsers(UserSearchRequest request);

    /**
     * 사용자 ID로 사용자 상세 정보를 조회한다.
     *
     * @param userId 조회할 사용자 ID
     * @return 사용자 상세 정보를 담은 HTTP 응답
     */
    @Operation(summary = "사용자 단건 조회", description = "사용자 ID로 사용자 상세 정보를 조회합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "사용자 단건 조회 성공",
            content = @Content(schema = @Schema(implementation = UserResponse.class))
    )
    ResponseEntity<ApiResponse<UserResponse>> getUser(
            @Parameter(description = "사용자 ID", required = true) String userId
    );

    /**
     * 신규 사용자를 등록한다.
     *
     * @param request 계정·인적 정보·권한·초기 비밀번호를 담은 생성 요청
     * @return 생성된 사용자 상세를 담은 HTTP 201 응답
     */
    @Operation(summary = "사용자 등록", description = "신규 사용자를 등록합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "사용자 등록 성공",
            content = @Content(schema = @Schema(implementation = UserResponse.class))
    )
    ResponseEntity<ApiResponse<UserResponse>> createUser(UserCreateRequest request);

    /**
     * 사용자 ID를 기준으로 사용자 정보를 수정한다.
     *
     * @param userId 수정할 사용자 ID
     * @param request 변경할 인적 정보·권한·계정 상태
     * @return 변경된 사용자 상세를 담은 HTTP 응답
     */
    @Operation(summary = "사용자 수정", description = "사용자 ID 기준으로 사용자 정보를 수정합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "사용자 수정 성공",
            content = @Content(schema = @Schema(implementation = UserResponse.class))
    )
    ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @Parameter(description = "사용자 ID", required = true) String userId,
            UserUpdateRequest request
    );

    /**
     * 사용자 ID를 기준으로 비밀번호를 초기화한다.
     *
     * @param userId 비밀번호를 변경할 사용자 ID
     * @param request 새 비밀번호를 담은 요청
     * @return 비밀번호 변경 후 사용자 상세를 담은 HTTP 응답
     */
    @Operation(summary = "사용자 비밀번호 초기화", description = "사용자 ID 기준으로 비밀번호를 초기화합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "사용자 비밀번호 초기화 성공",
            content = @Content(schema = @Schema(implementation = UserResponse.class))
    )
    ResponseEntity<ApiResponse<UserResponse>> resetPassword(
            @Parameter(description = "사용자 ID", required = true) String userId,
            UserPasswordResetRequest request
    );

    /**
     * 조회 조건에 맞는 사용자 목록을 Excel 파일로 다운로드한다.
     *
     * @param request 내보낼 사용자의 조회 조건
     * @param userId 다운로드 행위자로 기록할 인증 사용자 ID
     * @param clientIp 다운로드 이력에 기록할 클라이언트 IP
     * @return XLSX 바이트와 다운로드 헤더를 포함한 HTTP 응답
     */
    @Operation(summary = "사용자 Excel 다운로드", description = "조회 조건에 맞는 사용자 목록을 Excel 파일로 다운로드합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "사용자 Excel 다운로드 성공",
            content = @Content(
                    mediaType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    schema = @Schema(type = "string", format = "binary")
            )
    )
    ResponseEntity<byte[]> downloadExcel(
            UserSearchRequest request,
            @Parameter(hidden = true) String userId,
            @Parameter(hidden = true) String clientIp
    );
}
