package com.espay.admincore.application.service.auth;

import com.espay.admincore.application.dto.auth.AccessTokenSubject;
import com.espay.admincore.application.dto.auth.LoginResult;
import com.espay.admincore.application.exception.BusinessException;
import com.espay.admincore.application.port.out.auth.JwtTokenPort;
import com.espay.admincore.application.port.out.auth.RefreshTokenPort;
import com.espay.admincore.application.port.out.role.RolePersistencePort;
import com.espay.admincore.application.port.out.user.UserLookupPort;
import com.espay.admincore.domain.model.role.AdminRole;
import com.espay.admincore.domain.model.user.AdminUser;
import com.espay.admincore.domain.model.user.UserStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenSessionServiceTest {
    @Mock JwtTokenPort jwtTokenPort;
    @Mock RefreshTokenPort refreshTokenPort;
    @Mock UserLookupPort userLookupPort;
    @Mock RolePersistencePort rolePersistencePort;
    @InjectMocks TokenSessionService service;

    @Test
    void rotatesMatchingRefreshToken() {
        prepareValidRefreshToken();
        when(jwtTokenPort.issueAccessToken(new AccessTokenSubject("10", "admin01", "1")))
                .thenReturn("new-access-token");
        when(jwtTokenPort.issueRefreshToken("10")).thenReturn("new-refresh-token");
        when(jwtTokenPort.refreshTokenExpiresInMillis()).thenReturn(300_000L);
        when(jwtTokenPort.accessTokenExpiresInSeconds()).thenReturn(900L);
        when(refreshTokenPort.rotate(
                "10", "current-refresh-token", "new-refresh-token", 300_000L)).thenReturn(true);

        LoginResult result = service.refreshToken("current-refresh-token");

        assertThat(result.accessToken()).isEqualTo("new-access-token");
        assertThat(result.refreshToken()).isEqualTo("new-refresh-token");
        assertThat(result.refreshTokenExpiresInMillis()).isEqualTo(300_000L);
    }

    @Test
    void rejectsRefreshWhenAtomicRotationFails() {
        prepareValidRefreshToken();
        when(jwtTokenPort.issueAccessToken(new AccessTokenSubject("10", "admin01", "1")))
                .thenReturn("new-access-token");
        when(jwtTokenPort.issueRefreshToken("10")).thenReturn("new-refresh-token");
        when(jwtTokenPort.refreshTokenExpiresInMillis()).thenReturn(300_000L);
        when(refreshTokenPort.rotate(
                "10", "current-refresh-token", "new-refresh-token", 300_000L)).thenReturn(false);

        assertThatThrownBy(() -> service.refreshToken("current-refresh-token"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("리프레시 토큰이 일치하지 않습니다.");
    }

    @Test
    void rejectsMissingRefreshTokenBeforeCallingPorts() {
        assertThatThrownBy(() -> service.refreshToken(null))
                .isInstanceOf(BusinessException.class)
                .hasMessage("리프레시 토큰이 없습니다.");

        verifyNoInteractions(jwtTokenPort, refreshTokenPort, userLookupPort, rolePersistencePort);
    }

    @Test
    void deletesMatchingRefreshTokenOnLogout() {
        when(jwtTokenPort.isValid("refresh-token", "REFRESH")).thenReturn(true);
        when(jwtTokenPort.getUserId("refresh-token")).thenReturn("10");

        service.logout("refresh-token");

        verify(refreshTokenPort).deleteIfMatches("10", "refresh-token");
    }

    @Test
    void ignoresMissingOrInvalidRefreshTokenOnLogout() {
        service.logout(null);
        service.logout("");
        when(jwtTokenPort.isValid("invalid-token", "REFRESH")).thenReturn(false);
        service.logout("invalid-token");

        verifyNoInteractions(refreshTokenPort);
        verify(jwtTokenPort).isValid("invalid-token", "REFRESH");
    }

    private void prepareValidRefreshToken() {
        when(jwtTokenPort.isExpired("current-refresh-token")).thenReturn(false);
        when(jwtTokenPort.isValid("current-refresh-token", "REFRESH")).thenReturn(true);
        when(jwtTokenPort.getUserId("current-refresh-token")).thenReturn("10");
        when(userLookupPort.findById("10")).thenReturn(Optional.of(activeUser()));
        when(rolePersistencePort.findById("1")).thenReturn(Optional.of(activeRole()));
    }

    private AdminUser activeUser() {
        return AdminUser.reconstitute("10", "admin01", "관리자", "admin@example.com",
                null, null, "1", "encoded", "SECRET", null, UserStatus.ACTIVE,
                LocalDateTime.now(), LocalDateTime.now());
    }

    private AdminRole activeRole() {
        return AdminRole.reconstitute(
                "1", "운영자", null, true, LocalDateTime.now(), LocalDateTime.now());
    }
}
