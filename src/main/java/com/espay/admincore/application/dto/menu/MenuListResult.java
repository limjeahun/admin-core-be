package com.espay.admincore.application.dto.menu;

import java.util.List;

/**
 * 사용자 또는 권한에 대응하는 메뉴 조회 결과.
 *
 * @param items 정렬된 메뉴와 권한 정보 목록
 */
public record MenuListResult(List<MenuResult> items) {
}
