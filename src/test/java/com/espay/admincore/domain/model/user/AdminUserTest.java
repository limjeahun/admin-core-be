package com.espay.admincore.domain.model.user;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AdminUserTest {
    @Test
    void createsActiveUserWithoutOrganizationFields() {
        AdminUser user = AdminUser.create(
                "admin01", "관리자", "admin@example.com", null, "운영", "1", "encoded", null);

        assertThat(user.isActive()).isTrue();
        assertThat(user.hasOtpSecret()).isFalse();
        assertThat(user.getRoleId()).isEqualTo("1");
    }

    @Test
    void updatesOtpAndLastLoginWithoutChangingIdentity() {
        AdminUser user = AdminUser.create(
                "admin01", "관리자", "admin@example.com", null, null, "1", "encoded", UserStatus.ACTIVE);

        AdminUser updated = user.updateOtpSecret("SECRET");

        assertThat(updated.getLoginId()).isEqualTo(user.getLoginId());
        assertThat(updated.hasOtpSecret()).isTrue();
    }
}
