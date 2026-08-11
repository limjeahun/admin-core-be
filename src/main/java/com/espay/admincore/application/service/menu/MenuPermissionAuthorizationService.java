package com.espay.admincore.application.service.menu;

import com.espay.admincore.application.dto.menu.CheckMenuPermissionQuery;
import com.espay.admincore.application.port.in.menu.CheckMenuPermissionUseCase;
import com.espay.admincore.application.port.out.menu.RoleMenuPersistencePort;
import com.espay.admincore.application.port.out.role.RolePersistencePort;
import com.espay.admincore.application.port.out.user.UserLookupPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 요청 시점의 사용자·권한·메뉴 권한을 조회해 API 접근 여부를 판단하는 서비스.
 */
@Service
@RequiredArgsConstructor
public class MenuPermissionAuthorizationService implements CheckMenuPermissionUseCase {
    private final UserLookupPort userLookupPort;
    private final RolePersistencePort rolePersistencePort;
    private final RoleMenuPersistencePort roleMenuPersistencePort;

    /**
     * 활성 사용자와 권한이 후보 메뉴 중 하나에서 요청 작업을 허용하는지 확인한다.
     *
     * <p>잘못된 질의, 존재하지 않거나 비활성인 사용자·권한은 예외 대신 거부 결과를 반환한다.</p>
     *
     * @param query 사용자 ID, 후보 메뉴 코드와 조회·편집 작업
     * @return 하나 이상의 메뉴 권한이 작업을 허용하면 {@code true}
     */
    @Override
    @Transactional(readOnly = true)
    public boolean isAllowed(CheckMenuPermissionQuery query) {
        if (query == null || query.userId() == null || query.action() == null || query.menuCodes().isEmpty()) {
            return false;
        }
        var user = userLookupPort.findById(query.userId()).orElse(null);
        if (user == null || !user.isActive()) {
            return false;
        }
        var role = rolePersistencePort.findById(user.getRoleId()).orElse(null);
        if (role == null || !role.isActive()) {
            return false;
        }
        return roleMenuPersistencePort.findByRoleId(role.getId()).stream()
                .filter(permission -> query.menuCodes().contains(permission.menuCode()))
                .anyMatch(permission -> permission.allows(query.action()));
    }
}
