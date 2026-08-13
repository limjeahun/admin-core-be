package com.espay.admincore.domain.model.user;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

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
    void changesOnlyProfileFields() {
        AdminUser user = user();

        AdminUser changed = user.changeProfile(
                "변경 관리자", "changed@example.com", "010-2222-3333", "정산팀");

        assertThat(changed).isNotSameAs(user);
        assertThat(changed.getName()).isEqualTo("변경 관리자");
        assertThat(changed.getEmail()).isEqualTo("changed@example.com");
        assertThat(changed.getPhoneNo()).isEqualTo("010-2222-3333");
        assertThat(changed.getDeptName()).isEqualTo("정산팀");
        assertIdentityAndAuthenticationArePreserved(user, changed);
        assertThat(changed.getRoleId()).isEqualTo(user.getRoleId());
        assertThat(changed.getStatus()).isEqualTo(user.getStatus());
    }

    @Test
    void assignsOnlyRole() {
        AdminUser user = user();

        AdminUser changed = user.assignRole("2");

        assertThat(changed).isNotSameAs(user);
        assertThat(changed.getRoleId()).isEqualTo("2");
        assertThat(changed.getName()).isEqualTo(user.getName());
        assertThat(changed.getStatus()).isEqualTo(user.getStatus());
        assertIdentityAndAuthenticationArePreserved(user, changed);
    }

    @Test
    void activatesAndDeactivatesUser() {
        AdminUser inactive = user();

        AdminUser active = inactive.activate();
        AdminUser deactivated = active.deactivate();

        assertThat(active.isActive()).isTrue();
        assertThat(deactivated.isActive()).isFalse();
        assertThat(active.getName()).isEqualTo(inactive.getName());
        assertThat(deactivated.getRoleId()).isEqualTo(active.getRoleId());
        assertIdentityAndAuthenticationArePreserved(inactive, active);
        assertIdentityAndAuthenticationArePreserved(active, deactivated);
    }

    @Test
    void registersOtpSecretWithoutChangingIdentity() {
        AdminUser user = AdminUser.reconstitute(
                "10", "admin01", "관리자", "admin@example.com", null, null,
                "1", "encoded", null, null, UserStatus.ACTIVE,
                LocalDateTime.of(2026, 8, 1, 9, 0), LocalDateTime.of(2026, 8, 1, 9, 0));

        AdminUser changed = user.registerOtpSecret("SECRET");

        assertThat(changed.getLoginId()).isEqualTo(user.getLoginId());
        assertThat(changed.hasOtpSecret()).isTrue();
        assertThat(changed.getOtpSecret()).isEqualTo("SECRET");
    }

    @Test
    void recordsSuccessfulLoginWithoutChangingAuthenticationData() {
        AdminUser user = user();
        LocalDateTime loginAt = LocalDateTime.of(2026, 8, 13, 10, 30);

        AdminUser changed = user.recordSuccessfulLogin(loginAt);

        assertThat(changed.getLastLoginAt()).isEqualTo(loginAt);
        assertThat(changed.getPasswordHash()).isEqualTo(user.getPasswordHash());
        assertThat(changed.getOtpSecret()).isEqualTo(user.getOtpSecret());
        assertThat(changed.getCreatedAt()).isEqualTo(user.getCreatedAt());
    }

    private AdminUser user() {
        return AdminUser.reconstitute(
                "10", "admin01", "관리자", "admin@example.com", "010-1111-2222", "운영팀",
                "1", "encoded", "SECRET", LocalDateTime.of(2026, 8, 12, 15, 0), UserStatus.INACTIVE,
                LocalDateTime.of(2026, 8, 1, 9, 0), LocalDateTime.of(2026, 8, 2, 10, 0));
    }

    private void assertIdentityAndAuthenticationArePreserved(AdminUser before, AdminUser after) {
        assertThat(after.getId()).isEqualTo(before.getId());
        assertThat(after.getLoginId()).isEqualTo(before.getLoginId());
        assertThat(after.getPasswordHash()).isEqualTo(before.getPasswordHash());
        assertThat(after.getOtpSecret()).isEqualTo(before.getOtpSecret());
        assertThat(after.getLastLoginAt()).isEqualTo(before.getLastLoginAt());
        assertThat(after.getCreatedAt()).isEqualTo(before.getCreatedAt());
        assertThat(after.getUpdatedAt()).isAfterOrEqualTo(before.getUpdatedAt());
    }
}
