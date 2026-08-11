package com.espay.admincore.common.api;

import java.util.Map;

/**
 * API 실패 응답의 기계 판독 코드, 사용자 확인용 상세 메시지와 필드별 검증 오류를 표현한다.
 *
 * @param code 클라이언트가 오류 종류를 구분할 안정적인 코드
 * @param detail 오류 원인 또는 사용자 안내 메시지
 * @param fields 요청 필드 이름별 검증 오류 메시지
 */
public record ApiError(String code, String detail, Map<String, String> fields) {
}
