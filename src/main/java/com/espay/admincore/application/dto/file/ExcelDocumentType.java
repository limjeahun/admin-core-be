package com.espay.admincore.application.dto.file;

/**
 * 출력 어댑터가 적용할 관리자 Excel 문서 종류.
 */
public enum ExcelDocumentType {
    /** 관리자 사용자 목록 문서. */
    USERS,
    /** 로그인 및 OTP 인증 이력 문서. */
    LOGIN_HISTORY,
    /** 파일 업로드 및 다운로드 이력 문서. */
    FILE_HISTORY
}
