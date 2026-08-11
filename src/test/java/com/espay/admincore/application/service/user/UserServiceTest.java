package com.espay.admincore.application.service.user;

import com.espay.admincore.application.dto.user.CreateUserCommand;
import com.espay.admincore.application.port.out.auth.PasswordHashPort;
import com.espay.admincore.application.port.out.role.RolePersistencePort;
import com.espay.admincore.application.port.out.user.*;
import com.espay.admincore.domain.model.role.AdminRole;
import com.espay.admincore.domain.model.user.AdminUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
}
