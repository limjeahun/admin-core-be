package com.espay.admincore.common.api;

/**
 * 공통 API 실패 응답 생성에 필요한 HTTP 상태와 오류 정보.
 *
 * @param status 실제 HTTP 응답 상태 코드
 * @param code 클라이언트가 오류를 구분할 애플리케이션 코드
 * @param detail 오류 상세 메시지
 */
public record ApiFailure(int status, String code, String detail) {
}
