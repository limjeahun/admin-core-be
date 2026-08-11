package com.espay.admincore.adapter.in.web.user;

import com.espay.admincore.adapter.in.web.user.request.*;
import com.espay.admincore.adapter.in.web.user.response.UserListResponse;
import com.espay.admincore.adapter.in.web.user.response.UserResponse;
import com.espay.admincore.application.dto.history.ExcelDownloadResult;
import com.espay.admincore.application.dto.user.DownloadUsersExcelCommand;
import com.espay.admincore.application.port.in.user.UserCommandUseCase;
import com.espay.admincore.application.port.in.user.UserExportUseCase;
import com.espay.admincore.application.port.in.user.UserQueryUseCase;
import com.espay.admincore.common.api.ApiResponse;
import com.espay.admincore.adapter.in.web.resolver.ClientIp;
import com.espay.admincore.adapter.in.web.resolver.CurrentUserId;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 사용자 조회, 등록, 수정, 비밀번호 초기화 및 Excel 다운로드 요청을 처리한다.
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController implements UserSpec {
    private final UserQueryUseCase      queryUseCase;
    private final UserCommandUseCase    commandUseCase;
    private final UserExportUseCase     exportUseCase;

    /**
     * 검색 조건과 페이지 조건으로 사용자 목록을 조회한다.
     *
     * @param request 사용자 검색 조건
     * @return 사용자 목록이 포함된 HTTP 응답
     */
    @GetMapping
    @Override
    public ResponseEntity<ApiResponse<UserListResponse>> getUsers(@ModelAttribute UserSearchRequest request) {
        return ApiResponse.ok(
                UserListResponse.from(queryUseCase.getUsers(request.toQuery()))
        ).toResponseEntity();
    }

    /**
     * 사용자 ID로 사용자 상세 정보를 조회한다.
     *
     * @param userId 조회할 사용자 ID
     * @return 사용자 상세 정보가 포함된 HTTP 응답
     */
    @GetMapping("/{userId}")
    @Override
    public ResponseEntity<ApiResponse<UserResponse>> getUser(@PathVariable String userId) {
        return ApiResponse.ok(
                UserResponse.from(queryUseCase.getUser(userId))
        ).toResponseEntity();
    }

    /**
     * 신규 사용자를 등록한다.
     *
     * @param request 사용자 등록 정보
     * @return 등록된 사용자 정보가 포함된 HTTP 응답
     */
    @PostMapping
    @Override
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@Valid @RequestBody UserCreateRequest request) {
        return ApiResponse.created(
                UserResponse.from(
                        commandUseCase.createUser(request.toCommand())
                )
        ).toResponseEntity();
    }

    /**
     * 사용자 ID를 기준으로 사용자 정보를 수정한다.
     *
     * @param userId 수정할 사용자 ID
     * @param request 사용자 수정 정보
     * @return 수정된 사용자 정보가 포함된 HTTP 응답
     */
    @PutMapping("/{userId}")
    @Override
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable String userId,
            @Valid @RequestBody UserUpdateRequest request
    ) {
        return ApiResponse.ok(
                UserResponse.from(commandUseCase.updateUser(request.toCommand(userId)))
        ).toResponseEntity();
    }

    /**
     * 사용자 비밀번호를 새로운 비밀번호로 초기화한다.
     *
     * @param userId 비밀번호를 초기화할 사용자 ID
     * @param request 새로운 비밀번호 정보
     * @return 수정된 사용자 정보가 포함된 HTTP 응답
     */
    @PostMapping("/{userId}/password-reset")
    @Override
    public ResponseEntity<ApiResponse<UserResponse>> resetPassword(
            @PathVariable String userId,
            @Valid @RequestBody UserPasswordResetRequest request
    ) {
        return ApiResponse.ok(
                UserResponse.from(commandUseCase.changePassword(request.toCommand(userId)))
        ).toResponseEntity();
    }

    /**
     * 검색 조건에 맞는 사용자 목록을 Excel 파일로 반환한다.
     *
     * @param request 사용자 검색 조건
     * @param userId 다운로드를 요청한 사용자 ID
     * @param clientIp 요청 클라이언트 IP
     * @return Excel 파일 바이트가 포함된 HTTP 응답
     */
    @GetMapping("/excel")
    @Override
    public ResponseEntity<byte[]> downloadExcel(
            @ModelAttribute UserSearchRequest request,
            @CurrentUserId String userId,
            @Parameter(hidden = true) @ClientIp String clientIp
    ) {
        ExcelDownloadResult result = exportUseCase.downloadUsersExcel(
                DownloadUsersExcelCommand.of(request.toQuery(), userId, clientIp)
        );
        return ApiResponse.toExcelResponseEntity(result.fileName(), result.fileBytes());
    }
}
