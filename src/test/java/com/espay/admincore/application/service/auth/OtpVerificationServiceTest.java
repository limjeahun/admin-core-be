package com.espay.admincore.application.service.auth;

import com.espay.admincore.application.dto.auth.*;
import com.espay.admincore.application.exception.BusinessException;
import com.espay.admincore.application.port.out.auth.JwtTokenPort;
import com.espay.admincore.application.port.out.auth.OtpCodeVerifierPort;
import com.espay.admincore.application.port.out.auth.PreAuthStatePort;
import com.espay.admincore.application.port.out.auth.PreAuthTokenPort;
import com.espay.admincore.application.port.out.auth.RefreshTokenPort;
import com.espay.admincore.application.port.out.history.LoginHistoryPersistencePort;
import com.espay.admincore.application.port.out.role.RolePersistencePort;
import com.espay.admincore.application.port.out.user.UserLookupPort;
import com.espay.admincore.application.port.out.user.UserPersistencePort;
import com.espay.admincore.domain.model.role.AdminRole;
import com.espay.admincore.domain.model.user.AdminUser;
import com.espay.admincore.domain.model.user.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OtpVerificationServiceTest {
    @Mock PreAuthTokenPort preAuthTokenPort;
    @Mock PreAuthStatePort preAuthStatePort;
    @Mock UserLookupPort userLookupPort;
    @Mock RolePersistencePort rolePersistencePort;
    @Mock OtpCodeVerifierPort otpCodeVerifierPort;
    @Mock LoginHistoryPersistencePort loginHistoryPersistencePort;
    @Mock JwtTokenPort jwtTokenPort;
    @Mock RefreshTokenPort refreshTokenPort;
    @Mock UserPersistencePort userPersistencePort;

    private OtpVerificationService service;

    @BeforeEach
    void setUp() {
        service = new OtpVerificationService(
                preAuthTokenPort, preAuthStatePort, userLookupPort, rolePersistencePort,
                otpCodeVerifierPort, loginHistoryPersistencePort, jwtTokenPort,
                refreshTokenPort, userPersistencePort, new OtpPolicy("ESPAY", 240, 5));
    }

    @Test
    void issuesTokensAndCompletesLoginAfterValidOtp() {
        prepareAuthentication();
        when(otpCodeVerifierPort.verify("SECRET", "123456")).thenReturn(true);
        when(preAuthStatePort.consume(
                ConsumePreAuthStateCommand.of("jti-1", "10", PreAuthPurpose.OTP_LOGIN))).thenReturn(true);
        when(jwtTokenPort.issueAccessToken(new AccessTokenSubject("10", "admin01", "1")))
                .thenReturn("access-token");
        when(jwtTokenPort.issueRefreshToken("10")).thenReturn("refresh-token");
        when(jwtTokenPort.refreshTokenExpiresInMillis()).thenReturn(300_000L);
        when(jwtTokenPort.accessTokenExpiresInSeconds()).thenReturn(900L);

        LoginResult result = service.verifyOtp(command());

        assertThat(result.accessToken()).isEqualTo("access-token");
        assertThat(result.refreshToken()).isEqualTo("refresh-token");
        assertThat(result.refreshTokenExpiresInMillis()).isEqualTo(300_000L);
        verify(refreshTokenPort).save(SaveRefreshTokenCommand.of("10", "refresh-token", 300_000));
        verify(userPersistencePort).save(argThat(user -> user.getLastLoginAt() != null));
        verify(loginHistoryPersistencePort).save(argThat(history ->
                history.isSuccess() && "OTP".equals(history.getAuthStep())));
    }

    @Test
    void recordsFailureWithoutIssuingTokensForInvalidOtp() {
        prepareAuthentication();
        when(otpCodeVerifierPort.verify("SECRET", "123456")).thenReturn(false);
        when(preAuthStatePort.recordFailure("jti-1", 5)).thenReturn(1);

        assertThatThrownBy(() -> service.verifyOtp(command())).isInstanceOf(BusinessException.class);

        verify(loginHistoryPersistencePort).save(argThat(history ->
                !history.isSuccess() && "INVALID_OTP".equals(history.getFailReason())));
        verify(preAuthStatePort, never()).consume(any());
        verifyNoInteractions(jwtTokenPort, refreshTokenPort, userPersistencePort);
    }

    @Test
    void doesNotIssueTokensWhenPreAuthenticationStateWasAlreadyConsumed() {
        prepareAuthentication();
        when(otpCodeVerifierPort.verify("SECRET", "123456")).thenReturn(true);
        when(preAuthStatePort.consume(
                ConsumePreAuthStateCommand.of("jti-1", "10", PreAuthPurpose.OTP_LOGIN))).thenReturn(false);

        assertThatThrownBy(() -> service.verifyOtp(command())).isInstanceOf(BusinessException.class);

        verifyNoInteractions(jwtTokenPort, refreshTokenPort, userPersistencePort);
        verify(loginHistoryPersistencePort, never()).save(any());
    }

    private void prepareAuthentication() {
        PreAuthClaims claims = new PreAuthClaims("jti-1", "10", PreAuthPurpose.OTP_LOGIN);
        when(preAuthTokenPort.verify("pre-auth-token")).thenReturn(claims);
        when(preAuthStatePort.find("jti-1")).thenReturn(Optional.of(
                new PreAuthState("jti-1", "10", PreAuthPurpose.OTP_LOGIN, 0)));
        when(userLookupPort.findById("10")).thenReturn(Optional.of(activeUser()));
        when(rolePersistencePort.findById("1")).thenReturn(Optional.of(activeRole()));
        when(loginHistoryPersistencePort.findLatestLoginReason(any())).thenReturn("운영 점검");
    }

    private OtpVerifyCommand command() {
        return OtpVerifyCommand.of("pre-auth-token", "123456", "127.0.0.1", "JUnit");
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
