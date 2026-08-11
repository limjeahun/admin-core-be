package com.espay.admincore.common.api;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;

import java.nio.charset.StandardCharsets;

/**
 * 모든 REST API에서 사용하는 공통 응답 봉투.
 * 성공 시 {@code data}를, 실패 시 {@code error}를 포함하며 {@code code}는 실제 HTTP 상태와 맞춰 사용한다.
 *
 * @param code HTTP 상태에 대응하는 숫자 코드
 * @param message 요청 처리 결과를 설명하는 메시지
 * @param data 성공 시 반환할 응답 데이터
 * @param error 실패 시 반환할 구조화된 오류 정보
 * @param <T> 성공 응답 데이터 타입
 */
public record ApiResponse<T>(int code, String message, T data, ApiError error) {

    private static final MediaType EXCEL_CONTENT_TYPE = MediaType.parseMediaType(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    );

    /**
     * 데이터가 있는 HTTP 200 성공 응답 본문을 생성한다.
     *
     * @param data 반환할 성공 데이터
     * @param <T> 성공 데이터 타입
     * @return 성공 메시지와 데이터를 포함한 응답 객체
     */
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(200, "요청이 정상적으로 처리되었습니다.", data, null);
    }

    /**
     * 별도 데이터가 필요 없는 HTTP 200 성공 응답 본문을 생성한다.
     *
     * @return 데이터가 {@code null}인 성공 응답 객체
     */
    public static ApiResponse<Void> ok() {
        return ok(null);
    }

    /**
     * 신규 리소스 생성 결과를 담는 HTTP 201 응답 본문을 생성한다.
     *
     * @param data 생성된 리소스의 응답 데이터
     * @param <T> 생성 결과 데이터 타입
     * @return 생성 성공 메시지와 데이터를 포함한 응답 객체
     */
    public static <T> ApiResponse<T> created(T data) {
        return new ApiResponse<>(201, "요청한 정보가 생성되었습니다.", data, null);
    }

    /**
     * HTTP 상태와 애플리케이션 오류 코드를 포함한 실패 응답 본문을 생성한다.
     *
     * @param failure HTTP 상태, 오류 코드와 상세 메시지를 묶은 실패 정보
     * @return 데이터 없이 오류 정보를 포함한 응답 객체
     */
    public static ApiResponse<Void> error(ApiFailure failure) {
        return new ApiResponse<>(failure.status(), failure.detail(), null,
                new ApiError(failure.code(), failure.detail(), java.util.Map.of()));
    }

    /**
     * 현재 응답 객체의 {@link #code()}를 HTTP 상태로 사용하여 {@link ResponseEntity}로 변환한다.
     * 컨트롤러와 예외 처리기가 본문의 코드와 실제 HTTP 상태를 동일하게 유지할 수 있게 한다.
     *
     * @return HTTP 응답 객체
     */
    public ResponseEntity<ApiResponse<T>> toResponseEntity() {
        return ResponseEntity.status(this.code).body(this);
    }

    /**
     * 현재 응답 객체의 HTTP 상태와 Set-Cookie 헤더를 함께 적용한다.
     *
     * @param cookie 브라우저에 발급하거나 만료할 응답 쿠키
     * @return 상태, 쿠키 헤더와 공통 본문이 포함된 HTTP 응답
     */
    public ResponseEntity<ApiResponse<T>> toResponseEntity(ResponseCookie cookie) {
        return ResponseEntity.status(this.code)
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(this);
    }

    /**
     * 파일명과 Excel 바이트를 브라우저 다운로드용 HTTP 응답으로 변환한다.
     *
     * @param fileName 다운로드 대화상자에 표시할 파일명
     * @param fileBytes 응답 본문으로 전송할 Excel 파일 바이트
     * @return Excel 다운로드용 HTTP 응답
     */
    public static ResponseEntity<byte[]> toExcelResponseEntity(String fileName, byte[] fileBytes) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(fileName, StandardCharsets.UTF_8)
                        .build()
                        .toString())
                .contentType(EXCEL_CONTENT_TYPE)
                .body(fileBytes);
    }
}
