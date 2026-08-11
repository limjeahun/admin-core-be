package com.espay.admincore.application.service.auth;

import com.espay.admincore.application.dto.auth.*;
import com.espay.admincore.application.exception.BusinessException;
import com.espay.admincore.application.exception.ErrorCode;
import com.espay.admincore.application.port.out.auth.PasswordHashPort;
import com.espay.admincore.application.port.out.auth.PreAuthStatePort;
import com.espay.admincore.application.port.out.auth.PreAuthTokenPort;
import com.espay.admincore.application.port.out.history.LoginHistoryPersistencePort;
import com.espay.admincore.application.port.out.role.RolePersistencePort;
import com.espay.admincore.application.port.out.user.UserLookupPort;
import com.espay.admincore.domain.exception.InactiveUserException;
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
import static org.mockito.Mockito.*;

/**
 * 프레임워크 독립 로그인 유스케이스의 비밀번호 검증, 상태 검사와 감사 기록을 검증한다.
 */
@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    @Mock UserLookupPort userLookupPort;
    @Mock RolePersistencePort rolePersistencePort;
    @Mock PasswordHashPort passwordHashPort;
    @Mock PreAuthTokenPort preAuthTokenPort;
    @Mock PreAuthStatePort preAuthStatePort;
    @Mock LoginHistoryPersistencePort loginHistoryPersistencePort;

    private LoginService service;

    /**
     * 각 테스트가 애플리케이션 포트의 Mock만 사용하는 순수 로그인 서비스를 실행하도록 구성한다.
     */
    @BeforeEach
    void setUp() {
        service = new LoginService(userLookupPort, rolePersistencePort, passwordHashPort,
                preAuthTokenPort, preAuthStatePort, loginHistoryPersistencePort);
    }

    /**
     * 활성 계정의 비밀번호가 일치하면 OTP 사전 인증 상태와 성공 이력을 생성하는지 확인한다.
     */
    @Test
    void issuesPreAuthenticationChallengeForValidCredentials() {
        AdminUser user = activeUser();
        AdminRole role = activeRole();
        LoginCommand command = command();
        when(userLookupPort.findByLoginId("admin01")).thenReturn(Optional.of(user));
        when(rolePersistencePort.findById("1")).thenReturn(Optional.of(role));
        when(passwordHashPort.matches("Password1!", "encoded")).thenReturn(true);
        when(preAuthTokenPort.issue("10", PreAuthPurpose.OTP_LOGIN))
                .thenReturn(new IssuedPreAuthToken("pre-auth", "jti-1", 300));

        var result = service.login(command);

        assertThat(result.preAuthToken()).isEqualTo("pre-auth");
        assertThat(result.otpRegistered()).isFalse();
        verify(preAuthStatePort).create(
                CreatePreAuthStateCommand.of("jti-1", "10", PreAuthPurpose.OTP_LOGIN, 300));
        verify(loginHistoryPersistencePort).save(argThat(history ->
                "10".equals(history.getUserId())
                        && "LOGIN".equals(history.getAuthStep())
                        && history.isSuccess()
                        && "운영 점검".equals(history.getLoginReason())
                        && history.getFailReason() == null
                        && "admin01".equals(history.getInputId())
                        && "127.0.0.1".equals(history.getClientIp())
                        && "JUnit".equals(history.getUserAgent())));
    }

    /**
     * 비밀번호가 일치하지 않으면 토큰을 발급하지 않고 동일한 자격 증명 실패 코드로 기록하는지 확인한다.
     */
    @Test
    void rejectsInvalidPasswordWithoutIssuingToken() {
        when(userLookupPort.findByLoginId("admin01")).thenReturn(Optional.of(activeUser()));
        when(rolePersistencePort.findById("1")).thenReturn(Optional.of(activeRole()));
        when(passwordHashPort.matches("Password1!", "encoded")).thenReturn(false);

        assertThatThrownBy(() -> service.login(command()))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_CREDENTIALS));

        verify(loginHistoryPersistencePort).save(argThat(history ->
                history.getUserId() == null
                        && "LOGIN".equals(history.getAuthStep())
                        && !history.isSuccess()
                        && "운영 점검".equals(history.getLoginReason())
                        && "INVALID_CREDENTIALS".equals(history.getFailReason())
                        && "admin01".equals(history.getInputId())
                        && "127.0.0.1".equals(history.getClientIp())
                        && "JUnit".equals(history.getUserAgent())));
        verifyNoInteractions(preAuthTokenPort, preAuthStatePort);
    }

    /**
     * 비활성 사용자 도메인 규칙이 비밀번호 비교 전에 로그인을 차단하는지 확인한다.
     */
    @Test
    void rejectsInactiveUserBeforePasswordVerification() {
        AdminUser inactive = AdminUser.reconstitute("10", "admin01", "관리자", "admin@example.com",
                null, null, "1", "encoded", null, null, UserStatus.INACTIVE,
                LocalDateTime.now(), LocalDateTime.now());
        when(userLookupPort.findByLoginId("admin01")).thenReturn(Optional.of(inactive));

        assertThatThrownBy(() -> service.login(command())).isInstanceOf(InactiveUserException.class);

        verify(loginHistoryPersistencePort).save(argThat(history ->
                history.getUserId() == null
                        && "LOGIN".equals(history.getAuthStep())
                        && !history.isSuccess()
                        && "운영 점검".equals(history.getLoginReason())
                        && "ACCOUNT_LOCKED".equals(history.getFailReason())
                        && "admin01".equals(history.getInputId())
                        && "127.0.0.1".equals(history.getClientIp())
                        && "JUnit".equals(history.getUserAgent())));
        verifyNoInteractions(rolePersistencePort, passwordHashPort, preAuthTokenPort, preAuthStatePort);
    }

    /**
     * 테스트에서 공통으로 사용할 활성 관리자 사용자를 생성한다.
     *
     * @return 비밀번호 해시를 가진 활성 사용자
     */
    private AdminUser activeUser() {
        return AdminUser.reconstitute("10", "admin01", "관리자", "admin@example.com",
                null, null, "1", "encoded", null, null, UserStatus.ACTIVE,
                LocalDateTime.now(), LocalDateTime.now());
    }

    /**
     * 테스트에서 공통으로 사용할 활성 관리자 권한을 생성한다.
     *
     * @return 활성 권한
     */
    private AdminRole activeRole() {
        return AdminRole.reconstitute(
                "1", "운영자", null, true, LocalDateTime.now(), LocalDateTime.now());
    }

    /**
     * 테스트에서 공통으로 사용할 로그인 명령을 생성한다.
     *
     * @return 비밀번호와 감사 정보를 포함한 로그인 명령
     */
    private LoginCommand command() {
        return LoginCommand.of("admin01", "Password1!", "운영 점검", "127.0.0.1", "JUnit");
    }
}
