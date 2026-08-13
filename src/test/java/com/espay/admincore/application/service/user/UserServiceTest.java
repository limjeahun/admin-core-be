package com.espay.admincore.application.service.user;

import com.espay.admincore.application.dto.user.CreateUserCommand;
import com.espay.admincore.application.dto.user.UpdateUserCommand;
import com.espay.admincore.application.port.out.auth.PasswordHashPort;
import com.espay.admincore.application.port.out.role.RolePersistencePort;
import com.espay.admincore.application.port.out.user.*;
import com.espay.admincore.domain.model.role.AdminRole;
import com.espay.admincore.domain.model.user.AdminUser;
import com.espay.admincore.domain.model.user.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock UserLookupPort lookupPort;
    @Mock UserPersistencePort persistencePort;
    @Mock UserSearchPort searchPort;
    @Mock RolePersistencePort rolePort;
    @Mock PasswordHashPort passwordHashPort;
    UserService service;

    @BeforeEach
    void setUp() {
        service = new UserService(lookupPort, persistencePort, searchPort, rolePort, passwordHashPort);
    }

    @Test
    void createsAdminUserWithoutMerchantLookup() {
        AdminRole role = AdminRole.reconstitute(
                "1", "운영자", null, true, LocalDateTime.now(), LocalDateTime.now());
        when(lookupPort.existsByLoginId("admin01")).thenReturn(false);
        when(lookupPort.findByEmail("admin@example.com")).thenReturn(Optional.empty());
        when(rolePort.findById("1")).thenReturn(Optional.of(role));
        when(passwordHashPort.encode("Password1!")).thenReturn("encoded");
        when(persistencePort.save(any())).thenAnswer(invocation -> {
            AdminUser user = invocation.getArgument(0);
            return AdminUser.reconstitute("10", user.getLoginId(), user.getName(), user.getEmail(), user.getPhoneNo(), user.getDeptName(),
                    user.getRoleId(), user.getPasswordHash(), user.getOtpSecret(), user.getLastLoginAt(), user.getStatus(),
                    user.getCreatedAt(), user.getUpdatedAt());
        });

        var result = service.createUser(CreateUserCommand.of("admin01", "관리자", "admin@example.com",
                null, null, "1", "Password1!", "ACTIVE"));

        assertThat(result.userId()).isEqualTo("10");
        assertThat(result.roleName()).isEqualTo("운영자");
        verifyNoMoreInteractions(searchPort);
    }

    @Test
    void composesRequestedProfileRoleAndStatusChanges() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 8, 1, 9, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 8, 2, 10, 0);
        LocalDateTime lastLoginAt = LocalDateTime.of(2026, 8, 12, 15, 0);
        AdminUser user = AdminUser.reconstitute(
                "10", "admin01", "관리자", "admin@example.com", "010-1111-2222", "운영팀",
                "1", "encoded", "SECRET", lastLoginAt, UserStatus.ACTIVE, createdAt, updatedAt);
        AdminRole role = AdminRole.reconstitute(
                "2", "정산 담당자", null, true, createdAt, updatedAt);
        when(lookupPort.findById("10")).thenReturn(Optional.of(user));
        when(lookupPort.findByEmail("changed@example.com")).thenReturn(Optional.empty());
        when(rolePort.findById("2")).thenReturn(Optional.of(role));
        when(persistencePort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.updateUser(UpdateUserCommand.of(
                "10", "변경 관리자", "changed@example.com", "010-3333-4444", "정산팀", "2", "INACTIVE"));

        ArgumentCaptor<AdminUser> userCaptor = ArgumentCaptor.forClass(AdminUser.class);
        verify(persistencePort).save(userCaptor.capture());
        AdminUser changed = userCaptor.getValue();
        assertThat(changed.getName()).isEqualTo("변경 관리자");
        assertThat(changed.getEmail()).isEqualTo("changed@example.com");
        assertThat(changed.getPhoneNo()).isEqualTo("010-3333-4444");
        assertThat(changed.getDeptName()).isEqualTo("정산팀");
        assertThat(changed.getRoleId()).isEqualTo("2");
        assertThat(changed.getStatus()).isEqualTo(UserStatus.INACTIVE);
        assertThat(changed.getLoginId()).isEqualTo("admin01");
        assertThat(changed.getPasswordHash()).isEqualTo("encoded");
        assertThat(changed.getOtpSecret()).isEqualTo("SECRET");
        assertThat(changed.getLastLoginAt()).isEqualTo(lastLoginAt);
        assertThat(changed.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    void changesOnlyRequestedStatusAndKeepsProfile() {
        LocalDateTime value = LocalDateTime.of(2026, 8, 1, 9, 0);
        AdminUser user = AdminUser.reconstitute(
                "10", "admin01", "관리자", "admin@example.com", "010-1111-2222", "운영팀",
                "1", "encoded", "SECRET", null, UserStatus.ACTIVE, value, value);
        AdminRole role = AdminRole.reconstitute("1", "운영자", null, true, value, value);
        when(lookupPort.findById("10")).thenReturn(Optional.of(user));
        when(rolePort.findById("1")).thenReturn(Optional.of(role));
        when(persistencePort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.updateUser(UpdateUserCommand.of(
                "10", null, null, null, null, null, "INACTIVE"));

        ArgumentCaptor<AdminUser> userCaptor = ArgumentCaptor.forClass(AdminUser.class);
        verify(persistencePort).save(userCaptor.capture());
        AdminUser changed = userCaptor.getValue();
        assertThat(changed.getStatus()).isEqualTo(UserStatus.INACTIVE);
        assertThat(changed.getName()).isEqualTo(user.getName());
        assertThat(changed.getEmail()).isEqualTo(user.getEmail());
        assertThat(changed.getPhoneNo()).isEqualTo(user.getPhoneNo());
        assertThat(changed.getDeptName()).isEqualTo(user.getDeptName());
        assertThat(changed.getRoleId()).isEqualTo(user.getRoleId());
        verify(lookupPort, never()).findByEmail(any());
    }
}
