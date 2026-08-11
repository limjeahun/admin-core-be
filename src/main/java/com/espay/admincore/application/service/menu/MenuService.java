package com.espay.admincore.application.service.menu;

import com.espay.admincore.application.dto.menu.*;
import com.espay.admincore.application.port.in.menu.MenuQueryUseCase;
import com.espay.admincore.application.port.out.menu.MenuCatalogPort;
import com.espay.admincore.application.port.out.menu.RoleMenuPersistencePort;
import com.espay.admincore.application.port.out.role.RolePersistencePort;
import com.espay.admincore.application.port.out.user.UserLookupPort;
import com.espay.admincore.domain.exception.RoleNotFoundException;
import com.espay.admincore.domain.exception.UserNotFoundException;
import com.espay.admincore.domain.model.menu.AdminMenu;
import com.espay.admincore.domain.model.role.RoleMenuPermission;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 메뉴 카탈로그와 역할별 권한을 조합해 사용자 메뉴와 전체 메뉴를 조회하는 서비스.
 */
@Service
@RequiredArgsConstructor
public class MenuService implements MenuQueryUseCase {
    private final UserLookupPort            userLookupPort;
    private final RolePersistencePort       rolePersistencePort;
    private final MenuCatalogPort           menuCatalogPort;
    private final RoleMenuPersistencePort   roleMenuPersistencePort;

    /**
     * 사용자의 조회 가능 메뉴와 화면 계층 유지에 필요한 상위 메뉴를 반환한다.
     *
     * @param userId 메뉴를 조회할 사용자 ID
     * @return 사용자에게 노출할 메뉴와 조회·편집 상태
     */
    @Override
    @Transactional(readOnly = true)
    public MenuListResult getMyMenus(String userId) {
        // 1. 사용자와 사용자의 역할이 실제로 존재하는지 확인한다.
        var user = userLookupPort.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        rolePersistencePort.findById(user.getRoleId())
                .orElseThrow(() -> new RoleNotFoundException(user.getRoleId()));
        // 2. 역할의 메뉴 권한을 메뉴 코드로 바로 찾을 수 있는 형태로 만든다.
        Map<String, RoleMenuPermission> permissionMap = permissions(user.getRoleId());
        // 3. 조회 권한이 있는 메뉴 코드만 사용자 노출 대상으로 선택한다.
        Set<String> visibleCodes = permissionMap.values().stream()
                .filter(RoleMenuPermission::canView)
                .map(RoleMenuPermission::menuCode)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        // 4. 하위 메뉴가 화면에서 정상적으로 표시되도록 상위 메뉴도 노출 대상에 포함한다.
        menuCatalogPort.findByCodes(visibleCodes).stream()
                .map(AdminMenu::parentMenuCode)
                .filter(Objects::nonNull)
                .forEach(visibleCodes::add);
        // 5. 노출 대상 메뉴에 실제 조회·편집 권한을 결합해 응답 목록을 만든다.
        var items = menuCatalogPort.findByCodes(visibleCodes).stream()
                .map(menu -> {
                    RoleMenuPermission permission = permissionMap.get(menu.menuCode());
                    return MenuResult.from(
                            MenuResultSource.of(
                                    menu,
                                    true,
                                    permission == null || permission.canView(),
                                    permission != null && permission.canEdit()
                            )
                    );
                }).toList();
        return new MenuListResult(items);
    }

    /**
     * 권한 상태를 포함하지 않은 전체 메뉴 카탈로그를 반환한다.
     *
     * @return 모든 관리자 메뉴
     */
    @Override
    @Transactional(readOnly = true)
    public MenuListResult getActiveMenus() {
        return new MenuListResult(menuCatalogPort.findAll().stream()
                .map(menu -> MenuResult.from(
                            MenuResultSource.of(
                                    menu,
                                    false,
                                    false,
                                    false
                            )
                        )
                ).toList());
    }

    /**
     * 권한의 메뉴 목록을 메뉴 코드 기반 조회 맵으로 변환한다.
     *
     * @param roleId 권한 ID
     * @return 메뉴 코드를 키로 하는 권한 맵
     */
    private Map<String, RoleMenuPermission> permissions(String roleId) {
        return roleMenuPersistencePort.findByRoleId(roleId).stream()
                .collect(Collectors.toMap(RoleMenuPermission::menuCode, Function.identity()));
    }
}
