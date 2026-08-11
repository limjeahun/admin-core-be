package com.espay.admincore.application.service.menu;

import com.espay.admincore.application.dto.menu.CheckMenuPermissionQuery;
import com.espay.admincore.application.port.out.menu.RoleMenuPersistencePort;
import com.espay.admincore.application.port.out.role.RolePersistencePort;
import com.espay.admincore.application.port.out.user.UserLookupPort;
import com.espay.admincore.domain.model.menu.MenuPermissionAction;
import com.espay.admincore.domain.model.role.AdminRole;
import com.espay.admincore.domain.model.role.RoleMenuPermission;
import com.espay.admincore.domain.model.user.AdminUser;
import com.espay.admincore.domain.model.user.UserStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MenuPermissionAuthorizationServiceTest {
    @Test
    void evaluatesCurrentUserRoleAndMenuPermission() {
        UserLookupPort users = mock(UserLookupPort.class);
        RolePersistencePort roles = mock(RolePersistencePort.class);
        RoleMenuPersistencePort permissions = mock(RoleMenuPersistencePort.class);
        LocalDateTime now = LocalDateTime.now();
        AdminUser user = AdminUser.reconstitute("1", "admin", "관리자", "admin@example.com", null, null,
                "2", "encoded", null, null, UserStatus.ACTIVE, now, now);
        AdminRole role = AdminRole.reconstitute("2", "운영자", null, true, now, now);
        when(users.findById("1")).thenReturn(Optional.of(user));
        when(roles.findById("2")).thenReturn(Optional.of(role));
        when(permissions.findByRoleId("2")).thenReturn(List.of(
                new RoleMenuPermission("2", "USERS", true, false)));
        var service = new MenuPermissionAuthorizationService(users, roles, permissions);

        assertThat(service.isAllowed(CheckMenuPermissionQuery.of(
                "1", Set.of("USERS"), MenuPermissionAction.VIEW))).isTrue();
        assertThat(service.isAllowed(CheckMenuPermissionQuery.of(
                "1", Set.of("USERS"), MenuPermissionAction.EDIT))).isFalse();
    }
}
