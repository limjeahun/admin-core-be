package com.espay.admincore.application.service.auth;

import com.espay.admincore.application.exception.BusinessException;
import com.espay.admincore.application.exception.ErrorCode;
import com.espay.admincore.application.port.out.auth.JwtTokenPort;
import com.espay.admincore.application.port.out.role.RolePersistencePort;
import com.espay.admincore.application.port.out.user.UserLookupPort;
import com.espay.admincore.domain.exception.InactiveRoleException;
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
 * Access Token 인증 유스케이스가 JWT와 현재 사용자·권한 상태를 함께 검증하는지 확인한다.
 */
@ExtendWith(MockitoExtension.class)
class AccessTokenAuthenticationServiceTest {

    @Mock JwtTokenPort jwtTokenPort;
    @Mock UserLookupPort userLookupPort;
    @Mock RolePersistencePort rolePersistencePort;

    private AccessTokenAuthenticationService service;

    /**
     * 각 테스트가 애플리케이션 출력 포트의 Mock만 사용하는 순수 인증 서비스를 실행하도록 구성한다.
     */
    @BeforeEach
    void setUp() {
        service = new AccessTokenAuthenticationService(jwtTokenPort, userLookupPort, rolePersistencePort);
    }

    /**
     * 유효한 JWT의 사용자와 권한이 활성 상태이면 입력 어댑터용 인증 계정을 반환하는지 확인한다.
     */
    @Test
    void authenticatesCurrentActiveAccount() {
        when(jwtTokenPort.isValid("access", "ACCESS")).thenReturn(true);
        when(jwtTokenPort.getUserId("access")).thenReturn("10");
        when(userLookupPort.findById("10")).thenReturn(Optional.of(activeUser()));
        when(rolePersistencePort.findById("1")).thenReturn(Optional.of(role(true)));

        var result = service.authenticate("access");

        assertThat(result.userId()).isEqualTo("10");
        assertThat(result.loginId()).isEqualTo("admin01");
        assertThat(result.roleId()).isEqualTo("1");
    }

    /**
     * 서명이나 타입이 유효하지 않은 JWT는 DB 조회 없이 토큰 오류로 거부하는지 확인한다.
     */
    @Test
    void rejectsInvalidTokenBeforeAccountLookup() {
        when(jwtTokenPort.isValid("invalid", "ACCESS")).thenReturn(false);

        assertThatThrownBy(() -> service.authenticate("invalid"))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.TOKEN_INVALID));

        verifyNoInteractions(userLookupPort, rolePersistencePort);
    }

    /**
     * JWT가 유효해도 현재 권한이 비활성화되었다면 인증을 거부하는지 확인한다.
     */
    @Test
    void rejectsAccountWithInactiveCurrentRole() {
        when(jwtTokenPort.isValid("access", "ACCESS")).thenReturn(true);
        when(jwtTokenPort.getUserId("access")).thenReturn("10");
        when(userLookupPort.findById("10")).thenReturn(Optional.of(activeUser()));
        when(rolePersistencePort.findById("1")).thenReturn(Optional.of(role(false)));

        assertThatThrownBy(() -> service.authenticate("access")).isInstanceOf(InactiveRoleException.class);
    }

    /**
     * 테스트에서 사용할 활성 관리자 사용자를 생성한다.
     *
     * @return 활성 사용자
     */
    private AdminUser activeUser() {
        return AdminUser.reconstitute("10", "admin01", "관리자", "admin@example.com",
                null, null, "1", "encoded", null, null, UserStatus.ACTIVE,
                LocalDateTime.now(), LocalDateTime.now());
    }

    /**
     * 지정한 활성 상태의 테스트 권한을 생성한다.
     *
     * @param active 권한 활성 여부
     * @return 테스트 권한
     */
    private AdminRole role(boolean active) {
        return AdminRole.reconstitute(
                "1", "운영자", null, active, LocalDateTime.now(), LocalDateTime.now());
    }
}
