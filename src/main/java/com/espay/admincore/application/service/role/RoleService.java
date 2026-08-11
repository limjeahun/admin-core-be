package com.espay.admincore.application.service.role;

import com.espay.admincore.application.dto.menu.MenuListResult;
import com.espay.admincore.application.dto.menu.MenuPermissionCommand;
import com.espay.admincore.application.dto.menu.MenuResult;
import com.espay.admincore.application.dto.menu.MenuResultSource;
import com.espay.admincore.application.dto.role.*;
import com.espay.admincore.application.port.in.menu.RoleMenuUseCase;
import com.espay.admincore.application.port.in.role.RoleCommandUseCase;
import com.espay.admincore.application.port.in.role.RoleQueryUseCase;
import com.espay.admincore.application.port.out.menu.MenuCatalogPort;
import com.espay.admincore.application.port.out.menu.RoleMenuPersistencePort;
import com.espay.admincore.application.port.out.role.RolePersistencePort;
import com.espay.admincore.application.port.out.user.UserPersistencePort;
import com.espay.admincore.application.exception.BusinessException;
import com.espay.admincore.application.exception.ErrorCode;
import com.espay.admincore.domain.exception.RoleNotFoundException;
import com.espay.admincore.domain.model.role.AdminRole;
import com.espay.admincore.domain.model.role.RoleMenuPermission;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 관리자 권한의 생성·수정·조회와 역할별 메뉴 권한 관리를 처리하는 서비스.
 */
@Service
@RequiredArgsConstructor
public class RoleService implements RoleCommandUseCase, RoleQueryUseCase, RoleMenuUseCase {
    private final RolePersistencePort rolePersistencePort;
    private final UserPersistencePort userPersistencePort;
    private final MenuCatalogPort menuCatalogPort;
    private final RoleMenuPersistencePort roleMenuPersistencePort;

    /**
     * 권한명 중복을 검사하고 권한을 저장한 뒤 요청한 메뉴 권한을 연결한다.
     *
     * @param command 권한 기본 정보와 초기 메뉴 목록
     * @return 생성된 권한 결과
     */
    @Override
    @Transactional
    public RoleResult createRole(CreateRoleCommand command) {
        String name = command.name().trim();
        ensureNameAvailable(name, null);
        AdminRole saved = rolePersistencePort.save(
                AdminRole.create(name, command.description(), toActive(command.useYn())));
        replaceRoleMenus(saved.getId(), command.permissions());
        return RoleResult.from(saved, 0);
    }

    /**
     * 전체 메뉴에 특정 권한의 연결·조회·편집 여부를 조합한다.
     *
     * @param roleId 조회할 권한 ID
     * @return 메뉴 관리 화면에 표시할 전체 메뉴 상태
     */
    @Override
    @Transactional(readOnly = true)
    public MenuListResult getRoleMenus(String roleId) {
        find(roleId);
        Map<String, RoleMenuPermission> permissionMap = permissions(roleId);
        return new MenuListResult(menuCatalogPort.findAll().stream().map(menu -> {
            RoleMenuPermission permission = permissionMap.get(menu.menuCode());
            return MenuResult.from(MenuResultSource.of(menu, permission != null,
                    permission != null && permission.canView(), permission != null && permission.canEdit()));
        }).toList());
    }

    /**
     * 메뉴 코드와 중복, 최소 권한 규칙을 검증하고 권한의 메뉴 연결을 전체 교체한다.
     *
     * @param roleId 변경할 권한 ID
     * @param requestedPermissions 새로 적용할 메뉴별 권한 목록
     */
    @Override
    @Transactional
    public void updateRoleMenus(String roleId, List<MenuPermissionCommand> requestedPermissions) {
        find(roleId);
        replaceRoleMenus(roleId, requestedPermissions);
    }

    /**
     * 생략된 값은 기존 값으로 유지하며 권한명, 설명과 활성 상태를 수정한다.
     *
     * @param command 대상 권한 ID와 변경 정보
     * @return 수정된 권한과 현재 사용자 수
     */
    @Override
    @Transactional
    public RoleResult updateRole(UpdateRoleCommand command) {
        AdminRole role = find(command.roleId());
        String name = command.name() == null || command.name().isBlank() ? role.getName() : command.name().trim();
        ensureNameAvailable(name, role.getId());
        boolean active = command.useYn() == null || command.useYn().isBlank() ? role.isActive() : toActive(command.useYn());
        AdminRole saved = rolePersistencePort.save(role.update(
                name,
                command.description() == null ? role.getDescription() : command.description(),
                active));
        return RoleResult.from(saved, userPersistencePort.countByRoleId(saved.getId()));
    }

    /**
     * 권한 ID로 상세 정보와 해당 권한 사용자 수를 조회한다.
     *
     * @param roleId 조회할 권한 ID
     * @return 권한 상세 결과
     */
    @Override
    @Transactional(readOnly = true)
    public RoleResult getRole(String roleId) {
        AdminRole role = find(roleId);
        return RoleResult.from(role, userPersistencePort.countByRoleId(roleId));
    }

    /**
     * 권한 페이지를 조회하고 각 권한별 사용자 수를 조합한다.
     *
     * @param query 검색어와 페이지 조건
     * @return 권한 페이지 결과
     */
    @Override
    @Transactional(readOnly = true)
    public RoleListResult getRoles(RoleQuery query) {
        var items = rolePersistencePort.findPage(query).stream()
                .map(role -> RoleResult.from(
                            role,
                            userPersistencePort.countByRoleId(role.getId())
                        )
                ).toList();
        return new RoleListResult(items, rolePersistencePort.count(query), query.page(), query.size());
    }

    /**
     * 권한 ID로 권한을 찾고 없으면 도메인 예외를 발생시킨다.
     *
     * @param roleId 권한 ID
     * @return 조회된 권한
     */
    private AdminRole find(String roleId) {
        return rolePersistencePort.findById(roleId)
                .orElseThrow(() -> new RoleNotFoundException(roleId));
    }

    /**
     * 역할 메뉴 입력을 검증하고 저장할 도메인 권한 목록으로 변환해 전체 교체한다.
     *
     * <p>상위 그룹인 OPERATIONS는 저장하지 않고 사용자 메뉴 조회 시 자동 포함한다.</p>
     *
     * @param roleId 메뉴 권한을 교체할 역할 ID
     * @param requestedPermissions 새로 적용할 메뉴별 권한 목록
     */
    private void replaceRoleMenus(String roleId, List<MenuPermissionCommand> requestedPermissions) {
        List<MenuPermissionCommand> inputs = requestedPermissions == null ? List.of() : requestedPermissions;
        Set<String> codes = new HashSet<>();
        List<RoleMenuPermission> permissions = inputs.stream()
                .filter(input -> !"OPERATIONS".equalsIgnoreCase(input.menuCode()))
                .map(input -> {
                    String code = input.menuCode() == null ? "" : input.menuCode().trim().toUpperCase();
                    if (!menuCatalogPort.exists(code) || !codes.add(code)) {
                        throw new IllegalArgumentException("유효하지 않거나 중복된 메뉴 코드입니다: " + code);
                    }
                    if (!input.canView() && !input.canEdit()) {
                        throw new IllegalArgumentException("선택한 메뉴에는 조회 또는 편집 권한이 필요합니다.");
                    }
                    return new RoleMenuPermission(roleId, code, input.canView(), input.canEdit());
                }).toList();
        roleMenuPersistencePort.replaceRoleMenus(roleId, permissions);
    }

    /**
     * 역할의 메뉴 권한을 메뉴 코드 기반 조회 맵으로 변환한다.
     *
     * @param roleId 권한 ID
     * @return 메뉴 코드를 키로 하는 권한 맵
     */
    private Map<String, RoleMenuPermission> permissions(String roleId) {
        Map<String, RoleMenuPermission> result = new HashMap<>();
        roleMenuPersistencePort.findByRoleId(roleId)
                .forEach(permission -> result.put(permission.menuCode(), permission));
        return result;
    }

    /**
     * 다른 권한이 동일한 이름을 사용 중인지 확인한다.
     *
     * @param name 저장할 권한명
     * @param currentRoleId 수정 중인 권한 ID, 신규 생성이면 {@code null}
     */
    private void ensureNameAvailable(String name, String currentRoleId) {
        rolePersistencePort.findByName(name)
                .filter(role -> currentRoleId == null || !currentRoleId.equals(role.getId()))
                .ifPresent(role -> { throw new BusinessException(ErrorCode.ROLE_DUPLICATE, "이미 존재하는 권한명입니다."); });
    }

    /**
     * Y 또는 ACTIVE 표현을 활성 상태로 변환하고 그 외 값을 비활성으로 해석한다.
     *
     * @param useYn 사용 여부 표현값
     * @return 값이 없거나 활성 표현이면 {@code true}
     */
    private boolean toActive(String useYn) {
        return useYn == null || useYn.isBlank() || "Y".equalsIgnoreCase(useYn) || "ACTIVE".equalsIgnoreCase(useYn);
    }
}
