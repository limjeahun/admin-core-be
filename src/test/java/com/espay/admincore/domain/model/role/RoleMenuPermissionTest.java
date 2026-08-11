package com.espay.admincore.domain.model.role;

import com.espay.admincore.domain.model.menu.MenuPermissionAction;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RoleMenuPermissionTest {
    @Test
    void editPermissionAlsoAllowsView() {
        RoleMenuPermission permission = new RoleMenuPermission("1", "USERS", true, true);
        assertThat(permission.allows(MenuPermissionAction.VIEW)).isTrue();
        assertThat(permission.allows(MenuPermissionAction.EDIT)).isTrue();
    }

    @Test
    void rejectsEditWithoutView() {
        assertThatThrownBy(() -> new RoleMenuPermission("1", "USERS", false, true))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
