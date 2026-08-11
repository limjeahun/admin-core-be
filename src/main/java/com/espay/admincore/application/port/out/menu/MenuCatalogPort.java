package com.espay.admincore.application.port.out.menu;

import com.espay.admincore.domain.model.menu.AdminMenu;

import java.util.Collection;
import java.util.List;

/**
 * 코드에 정의된 관리자 메뉴 카탈로그를 조회하는 출력 포트.
 */
public interface MenuCatalogPort {
    /**
     * 애플리케이션에서 지원하는 관리자 메뉴를 카탈로그 선언 순서로 조회한다.
     *
     * @return 정렬된 전체 관리자 메뉴 목록
     */
    List<AdminMenu> findAll();
    /**
     * 지정한 코드에 해당하는 메뉴만 조회한다.
     *
     * @param menuCodes 조회할 메뉴 코드 모음
     * @return 카탈로그 선언 순서의 일치 메뉴 목록
     */
    List<AdminMenu> findByCodes(Collection<String> menuCodes);
    /**
     * 지원하는 메뉴 코드인지 확인한다.
     *
     * @param menuCode 확인할 메뉴 코드
     * @return 존재하면 {@code true}
     */
    boolean exists(String menuCode);
}
