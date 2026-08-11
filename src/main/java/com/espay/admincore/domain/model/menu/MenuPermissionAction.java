package com.espay.admincore.domain.model.menu;

/**
 * 메뉴 기반 접근 제어에서 검사할 작업 종류.
 */
public enum MenuPermissionAction {
    /** 목록·상세·파일 조회와 같은 읽기 작업. */
    VIEW,
    /** 등록·수정·비밀번호 초기화와 같은 변경 작업. */
    EDIT
}
