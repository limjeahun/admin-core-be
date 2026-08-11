package com.espay.admincore.adapter.in.web;

import com.espay.admincore.application.exception.BusinessException;
import com.espay.admincore.application.exception.ErrorCode;
import com.espay.admincore.common.api.ApiError;
import com.espay.admincore.common.api.ApiFailure;
import com.espay.admincore.common.api.ApiResponse;
import com.espay.admincore.domain.exception.InactiveRoleException;
import com.espay.admincore.domain.exception.InactiveUserException;
import com.espay.admincore.domain.exception.RoleNotFoundException;
import com.espay.admincore.domain.exception.UserNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 컨트롤러까지 전파된 예외를 공통 {@link ApiResponse} 형식과 적절한 HTTP 상태로 변환한다.
 * 예상 가능한 업무·인증·검증 오류는 구체적으로 처리하고, 그 밖의 예외는 서버 로그에 남긴 뒤
 * 내부 구현 정보가 노출되지 않는 일반 오류 메시지로 응답한다.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 업무 예외가 보유한 {@link ErrorCode}의 HTTP 상태와 오류 코드를 그대로 응답에 반영한다.
     *
     * @param exception 애플리케이션 계층에서 발생한 예상 가능한 업무 예외
     * @return 공통 형식으로 변환된 업무 오류 HTTP 응답
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException exception) {
        ErrorCode errorCode = exception.getErrorCode();
        return ApiResponse.error(new ApiFailure(
                status(errorCode).value(), errorCode.getCode(), exception.getMessage())).toResponseEntity();
    }

    /**
     * 사용자 조회 도메인 예외를 사용자 없음 API 오류로 변환한다.
     *
     * @param exception 조회에 실패한 사용자 정보를 가진 도메인 예외
     * @return 사용자 없음 404 HTTP 응답
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleUserNotFound(UserNotFoundException exception) {
        return ApiResponse.error(new ApiFailure(
                404, ErrorCode.USER_NOT_FOUND.getCode(), exception.getMessage())).toResponseEntity();
    }

    /**
     * 권한 조회 도메인 예외를 권한 없음 API 오류로 변환한다.
     *
     * @param exception 조회에 실패한 권한 정보를 가진 도메인 예외
     * @return 권한 없음 404 HTTP 응답
     */
    @ExceptionHandler(RoleNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleRoleNotFound(RoleNotFoundException exception) {
        return ApiResponse.error(new ApiFailure(
                404, ErrorCode.ROLE_NOT_FOUND.getCode(), exception.getMessage())).toResponseEntity();
    }

    /**
     * 비활성 사용자 또는 권한 도메인 예외를 계정 잠금 오류로 변환한다.
     *
     * @param exception 인증을 거부한 도메인 예외
     * @return 계정 잠금 403 HTTP 응답
     */
    @ExceptionHandler({InactiveUserException.class, InactiveRoleException.class})
    public ResponseEntity<ApiResponse<Void>> handleInactiveAccount(RuntimeException exception) {
        return ApiResponse.error(new ApiFailure(
                403, ErrorCode.ACCOUNT_LOCKED.getCode(), exception.getMessage())).toResponseEntity();
    }

    /**
     * 값 객체나 변환 로직에서 발생한 잘못된 인자 예외를 입력 오류 응답으로 변환한다.
     *
     * @param exception 유효하지 않은 인자에 대한 상세 메시지를 가진 예외
     * @return 입력 오류를 나타내는 400 HTTP 응답
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException exception) {
        return ApiResponse.error(new ApiFailure(400, "INVALID_INPUT", exception.getMessage())).toResponseEntity();
    }

    /**
     * Bean Validation 오류를 필드 이름별 첫 번째 메시지로 정리하여 구조화된 오류 응답을 만든다.
     *
     * @param exception 컨트롤러 요청 객체 검증에서 발생한 예외
     * @return 필드별 오류 맵을 포함한 400 HTTP 응답
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException exception) {
        Map<String, String> fields = new LinkedHashMap<>();
        for (FieldError error : exception.getBindingResult().getFieldErrors()) {
            fields.putIfAbsent(error.getField(), error.getDefaultMessage());
        }
        ApiResponse<Void> body = new ApiResponse<>(
                400,
                "입력값을 확인해 주세요.",
                null,
                new ApiError("INVALID_INPUT", "입력값을 확인해 주세요.", fields)
        );
        return body.toResponseEntity();
    }

    /**
     * 별도 처리되지 않은 예외를 요청 추적 로그에 기록하고 일반화된 500 응답으로 변환한다.
     *
     * @param exception 예상하지 못한 처리 예외
     * @return 내부 상세를 숨긴 500 HTTP 응답
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception exception) {
        log.error("unexpected error", exception);
        return ApiResponse.error(new ApiFailure(
                500, "INTERNAL_ERROR", "서버 내부 오류가 발생했습니다.")).toResponseEntity();
    }

    /**
     * 프레임워크 독립적인 애플리케이션 오류 코드를 HTTP 상태로 변환한다.
     *
     * @param errorCode 애플리케이션 오류 코드
     * @return 입력 어댑터에서 사용할 HTTP 상태
     */
    private HttpStatus status(ErrorCode errorCode) {
        return switch (errorCode) {
            case INVALID_INPUT -> HttpStatus.BAD_REQUEST;
            case INVALID_CREDENTIALS, UNAUTHORIZED, TOKEN_INVALID, TOKEN_EXPIRED -> HttpStatus.UNAUTHORIZED;
            case ACCOUNT_LOCKED, ACCESS_DENIED -> HttpStatus.FORBIDDEN;
            case USER_NOT_FOUND, ROLE_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case USER_DUPLICATE, ROLE_DUPLICATE -> HttpStatus.CONFLICT;
            case INTERNAL_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}
