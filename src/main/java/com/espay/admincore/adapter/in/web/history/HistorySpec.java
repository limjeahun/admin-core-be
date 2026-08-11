package com.espay.admincore.adapter.in.web.history;

import com.espay.admincore.adapter.in.web.history.request.FileHistorySearchRequest;
import com.espay.admincore.adapter.in.web.history.request.LoginHistorySearchRequest;
import com.espay.admincore.adapter.in.web.history.response.FileHistoryListResponse;
import com.espay.admincore.adapter.in.web.history.response.LoginHistoryListResponse;
import com.espay.admincore.common.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

/**
 * 로그인 이력, 파일 이력 및 이력 Excel 다운로드 API의 Swagger 명세.
 */
@Tag(name = "이력 관리 API", description = "로그인 이력, 파일 이력, 이력 Excel 다운로드 API")
public interface HistorySpec {

    /**
     * 검색 조건과 페이지 조건으로 로그인 이력을 조회한다.
     *
     * @param request 기간·인증 단계·성공 여부·검색어·페이지 조건
     * @return 현재 페이지의 로그인 이력과 전체 건수를 담은 HTTP 응답
     */
    @Operation(
            summary = "로그인/인증 이력 목록 조회",
            description = "조회 기간, 인증 단계, 성공 여부, 검색어, 페이지 조건으로 로그인/인증 이력을 조회합니다."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "로그인/인증 이력 목록 조회 성공",
            content = @Content(schema = @Schema(implementation = LoginHistoryListResponse.class))
    )
    ResponseEntity<ApiResponse<LoginHistoryListResponse>> getLoginHistory(LoginHistorySearchRequest request);

    /**
     * 검색 조건과 페이지 조건으로 파일 처리 이력을 조회한다.
     *
     * @param request 기간·입출력 구분·성공 여부·검색어·페이지 조건
     * @return 현재 페이지의 파일 이력과 전체 건수를 담은 HTTP 응답
     */
    @Operation(
            summary = "파일 이력 목록 조회",
            description = "조회 기간, 입출력 구분, 성공 여부, 검색어, 페이지 조건으로 파일 이력을 조회합니다."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "파일 이력 목록 조회 성공",
            content = @Content(schema = @Schema(implementation = FileHistoryListResponse.class))
    )
    ResponseEntity<ApiResponse<FileHistoryListResponse>> getFileHistory(FileHistorySearchRequest request);

    /**
     * 조회 조건에 맞는 로그인 이력을 Excel 파일로 다운로드한다.
     *
     * @param request 내보낼 로그인 이력의 조회 조건
     * @param userId 다운로드 행위자로 기록할 인증 사용자 ID
     * @param clientIp 다운로드 이력에 기록할 클라이언트 IP
     * @return XLSX 바이트와 다운로드 헤더를 포함한 HTTP 응답
     */
    @Operation(
            summary = "로그인/인증 이력 Excel 다운로드",
            description = "조회 조건에 맞는 로그인/인증 이력을 Excel 파일로 다운로드합니다."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "로그인/인증 이력 Excel 다운로드 성공",
            content = @Content(
                    mediaType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    schema = @Schema(type = "string", format = "binary")
            )
    )
    ResponseEntity<byte[]> downloadLoginHistory(
            LoginHistorySearchRequest request,
            @Parameter(hidden = true) String userId,
            @Parameter(hidden = true) String clientIp
    );

    /**
     * 조회 조건에 맞는 파일 이력을 Excel 파일로 다운로드한다.
     *
     * @param request 내보낼 파일 이력의 조회 조건
     * @param userId 다운로드 행위자로 기록할 인증 사용자 ID
     * @param clientIp 다운로드 이력에 기록할 클라이언트 IP
     * @return XLSX 바이트와 다운로드 헤더를 포함한 HTTP 응답
     */
    @Operation(
            summary = "파일 이력 Excel 다운로드",
            description = "조회 조건에 맞는 파일 이력을 Excel 파일로 다운로드합니다."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "파일 이력 Excel 다운로드 성공",
            content = @Content(
                    mediaType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    schema = @Schema(type = "string", format = "binary")
            )
    )
    ResponseEntity<byte[]> downloadFileHistory(
            FileHistorySearchRequest request,
            @Parameter(hidden = true) String userId,
            @Parameter(hidden = true) String clientIp
    );
}
