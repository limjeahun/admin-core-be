package com.espay.admincore.adapter.in.web.history;

import com.espay.admincore.adapter.in.web.history.request.FileHistorySearchRequest;
import com.espay.admincore.adapter.in.web.history.request.LoginHistorySearchRequest;
import com.espay.admincore.adapter.in.web.history.response.FileHistoryListResponse;
import com.espay.admincore.adapter.in.web.history.response.LoginHistoryListResponse;
import com.espay.admincore.application.dto.history.DownloadHistoryExcelCommand;
import com.espay.admincore.application.dto.history.ExcelDownloadResult;
import com.espay.admincore.application.port.in.history.FileHistoryQueryUseCase;
import com.espay.admincore.application.port.in.history.HistoryExportUseCase;
import com.espay.admincore.application.port.in.history.LoginHistoryQueryUseCase;
import com.espay.admincore.common.api.ApiResponse;
import com.espay.admincore.adapter.in.web.resolver.ClientIp;
import com.espay.admincore.adapter.in.web.resolver.CurrentUserId;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 로그인 이력과 파일 이력 조회 및 Excel 다운로드 요청을 처리한다.
 */
@RestController
@RequestMapping("/api/v1/history")
@RequiredArgsConstructor
public class HistoryController implements HistorySpec {
    private final LoginHistoryQueryUseCase  loginHistoryQueryUseCase;
    private final FileHistoryQueryUseCase   fileHistoryQueryUseCase;
    private final HistoryExportUseCase      exportUseCase;

    /**
     * 검색 조건과 페이지 조건으로 로그인 및 인증 이력을 조회한다.
     *
     * @param request 로그인 이력 검색 조건
     * @return 로그인 이력 목록이 포함된 HTTP 응답
     */
    @GetMapping("/login")
    @Override
    public ResponseEntity<ApiResponse<LoginHistoryListResponse>> getLoginHistory(
            @ModelAttribute LoginHistorySearchRequest request
    ) {
        return ApiResponse.ok(
                LoginHistoryListResponse.from(
                        loginHistoryQueryUseCase.getLoginHistory(request.toQuery())
                )
        ).toResponseEntity();
    }

    /**
     * 검색 조건과 페이지 조건으로 파일 처리 이력을 조회한다.
     *
     * @param request 파일 이력 검색 조건
     * @return 파일 이력 목록이 포함된 HTTP 응답
     */
    @GetMapping("/file")
    @Override
    public ResponseEntity<ApiResponse<FileHistoryListResponse>> getFileHistory(
            @ModelAttribute FileHistorySearchRequest request
    ) {
        return ApiResponse.ok(
                FileHistoryListResponse.from(
                        fileHistoryQueryUseCase.getFileHistory(request.toQuery())
                )
        ).toResponseEntity();
    }

    /**
     * 검색 조건에 맞는 로그인 및 인증 이력을 Excel 파일로 반환한다.
     *
     * @param request 로그인 이력 검색 조건
     * @param userId 다운로드를 요청한 사용자 ID
     * @param clientIp 요청 클라이언트 IP
     * @return Excel 파일 바이트가 포함된 HTTP 응답
     */
    @GetMapping("/login/excel")
    @Override
    public ResponseEntity<byte[]> downloadLoginHistory(
            @ModelAttribute LoginHistorySearchRequest request,
            @CurrentUserId String userId,
            @Parameter(hidden = true) @ClientIp String clientIp
    ) {
        ExcelDownloadResult result = exportUseCase.downloadLoginHistoryExcel(
                DownloadHistoryExcelCommand.of(request.toQuery(), userId, clientIp)
        );
        return ApiResponse.toExcelResponseEntity(result.fileName(), result.fileBytes());
    }

    /**
     * 검색 조건에 맞는 파일 처리 이력을 Excel 파일로 반환한다.
     *
     * @param request 파일 이력 검색 조건
     * @param userId 다운로드를 요청한 사용자 ID
     * @param clientIp 요청 클라이언트 IP
     * @return Excel 파일 바이트가 포함된 HTTP 응답
     */
    @GetMapping("/file/excel")
    @Override
    public ResponseEntity<byte[]> downloadFileHistory(@ModelAttribute FileHistorySearchRequest request,
                                                       @CurrentUserId String userId,
                                                       @Parameter(hidden = true) @ClientIp String clientIp) {
        ExcelDownloadResult result = exportUseCase.downloadFileHistoryExcel(
                DownloadHistoryExcelCommand.of(request.toQuery(), userId, clientIp)
        );
        return ApiResponse.toExcelResponseEntity(result.fileName(), result.fileBytes());
    }
}
