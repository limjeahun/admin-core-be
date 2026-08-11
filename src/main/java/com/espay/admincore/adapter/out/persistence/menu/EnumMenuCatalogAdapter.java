package com.espay.admincore.adapter.out.persistence.menu;

import com.espay.admincore.application.port.out.menu.MenuCatalogPort;
import com.espay.admincore.domain.model.menu.AdminMenu;
import com.espay.admincore.domain.model.menu.AdminMenuDefinition;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

/**
 * DB 대신 {@link AdminMenuDefinition} 열거형을 관리자 메뉴 카탈로그로 사용하는 어댑터.
 */
@Component
public class EnumMenuCatalogAdapter implements MenuCatalogPort {
    /**
     * 열거형에 선언된 전체 관리자 메뉴를 표시 순서가 보장된 도메인 목록으로 변환한다.
     *
     * @return 코드에 정의된 전체 메뉴
     */
    @Override
    public List<AdminMenu> findAll() {
        return AdminMenuDefinition.findAll();
    }

    /**
     * 요청한 메뉴 코드만 열거형 카탈로그에서 찾아 선언 순서대로 반환한다.
     *
     * @param menuCodes 조회할 메뉴 코드
     * @return 코드에 일치하는 메뉴 목록
     */
    @Override
    public List<AdminMenu> findByCodes(Collection<String> menuCodes) {
        return AdminMenuDefinition.findByCodes(menuCodes);
    }

    /**
     * 메뉴 권한 저장 전에 전달된 코드가 현재 카탈로그에서 지원되는 값인지 확인한다.
     *
     * @param menuCode 확인할 메뉴 코드
     * @return 열거형에 존재하면 {@code true}
     */
    @Override
    public boolean exists(String menuCode) {
        return AdminMenuDefinition.exists(menuCode);
    }
}
