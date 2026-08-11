package com.espay.admincore.application.service.role;

import com.espay.admincore.application.dto.menu.MenuPermissionCommand;
import com.espay.admincore.application.dto.role.CreateRoleCommand;
import com.espay.admincore.application.port.out.menu.MenuCatalogPort;
import com.espay.admincore.application.port.out.menu.RoleMenuPersistencePort;
import com.espay.admincore.application.port.out.role.RolePersistencePort;
import com.espay.admincore.application.port.out.user.UserPersistencePort;
import com.espay.admincore.domain.model.menu.AdminMenu;
import com.espay.admincore.domain.model.role.AdminRole;
import com.espay.admincore.domain.model.role.RoleMenuPermission;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {
    @Mock RolePersistencePort rolePersistencePort;
    @Mock UserPersistencePort userPersistencePort;
    @Mock MenuCatalogPort menuCatalogPort;
    @Mock RoleMenuPersistencePort roleMenuPersistencePort;

    private RoleService service;

    @BeforeEach
    void setUp() {
        service = new RoleService(
                rolePersistencePort, userPersistencePort, menuCatalogPort, roleMenuPersistencePort);
    }

    @Test
    void createsRoleAndStoresInitialMenuPermissions() {
        AdminRole saved = activeRole();
        when(rolePersistencePort.findByName("운영자")).thenReturn(Optional.empty());
        when(rolePersistencePort.save(any())).thenReturn(saved);
        when(menuCatalogPort.exists("USERS")).thenReturn(true);

        service.createRole(CreateRoleCommand.of(
                " 운영자 ", "운영 권한", "Y",
                List.of(MenuPermissionCommand.of(" users ", true, false))));

        verify(roleMenuPersistencePort).replaceRoleMenus(eq("1"), argThat(permissions ->
                permissions.size() == 1
                        && "1".equals(permissions.get(0).roleId())
                        && "USERS".equals(permissions.get(0).menuCode())
                        && permissions.get(0).canView()));
    }

    @Test
    void returnsRoleMenuConnectionAndPermissions() {
        when(rolePersistencePort.findById("1")).thenReturn(Optional.of(activeRole()));
        when(menuCatalogPort.findAll()).thenReturn(List.of(
                new AdminMenu("USERS", "사용자", "/users", null, 1),
                new AdminMenu("ROLES", "권한", "/roles", null, 2)));
        when(roleMenuPersistencePort.findByRoleId("1")).thenReturn(List.of(
                new RoleMenuPermission("1", "USERS", true, false)));

        var result = service.getRoleMenus("1");

        assertThat(result.items()).hasSize(2);
        assertThat(result.items().get(0).assigned()).isTrue();
        assertThat(result.items().get(0).canView()).isTrue();
        assertThat(result.items().get(1).assigned()).isFalse();
    }

    @Test
    void rejectsDuplicateMenuCodesWhenUpdatingRoleMenus() {
        when(rolePersistencePort.findById("1")).thenReturn(Optional.of(activeRole()));
        when(menuCatalogPort.exists("USERS")).thenReturn(true);

        assertThatThrownBy(() -> service.updateRoleMenus("1", List.of(
                MenuPermissionCommand.of("USERS", true, false),
                MenuPermissionCommand.of("users", true, true))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("유효하지 않거나 중복된 메뉴 코드입니다: USERS");

        verify(roleMenuPersistencePort, never()).replaceRoleMenus(anyString(), anyList());
    }

    private AdminRole activeRole() {
        return AdminRole.reconstitute(
                "1", "운영자", "운영 권한", true, LocalDateTime.now(), LocalDateTime.now());
    }
}
