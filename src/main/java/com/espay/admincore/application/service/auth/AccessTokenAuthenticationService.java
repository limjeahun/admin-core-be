package com.espay.admincore.application.service.auth;

import com.espay.admincore.application.dto.auth.AuthenticatedAccount;
import com.espay.admincore.application.port.in.auth.AccessTokenAuthenticationUseCase;
import com.espay.admincore.application.port.out.auth.JwtTokenPort;
import com.espay.admincore.application.port.out.role.RolePersistencePort;
import com.espay.admincore.application.port.out.user.UserLookupPort;
import com.espay.admincore.application.exception.BusinessException;
import com.espay.admincore.application.exception.ErrorCode;
import com.espay.admincore.domain.exception.InactiveRoleException;
import com.espay.admincore.domain.exception.InactiveUserException;
import com.espay.admincore.domain.model.role.AdminRole;
import com.espay.admincore.domain.model.user.AdminUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Access Token의 무결성과 현재 사용자·권한 상태를 검증하는 애플리케이션 서비스.
 * HTTP 헤더 및 Spring SecurityContext와 무관한 인증 결과만 입력 어댑터에 반환한다.
 */
@Service
@RequiredArgsConstructor
public class AccessTokenAuthenticationService implements AccessTokenAuthenticationUseCase {

    private final JwtTokenPort          jwtTokenPort;
    private final UserLookupPort        userLookupPort;
    private final RolePersistencePort   rolePersistencePort;

    /**
     * JWT 검증 후 토큰 주체의 사용자와 권한을 다시 조회하여 현재 접근 가능 상태를 확인한다.
     *
     * @param accessToken 검증할 Bearer Access Token
     * @return 현재 상태가 반영된 인증 계정
     * @throws BusinessException 토큰이 유효하지 않거나 사용자·권한을 확인할 수 없는 경우
     * @throws InactiveUserException 현재 사용자가 비활성 상태인 경우
     * @throws InactiveRoleException 현재 권한이 비활성 상태인 경우
     */
    @Override
    @Transactional(readOnly = true)
    public AuthenticatedAccount authenticate(String accessToken) {
        if (!jwtTokenPort.isValid(accessToken, "ACCESS")) {
            throw invalid();
        }

        String userId = jwtTokenPort.getUserId(accessToken);
        AdminUser user = userLookupPort.findById(userId).orElseThrow(this::invalid);
        user.validateLoginAllowed();

        AdminRole role = rolePersistencePort.findById(user.getRoleId()).orElseThrow(this::invalid);
        role.validateLoginAllowed();

        return new AuthenticatedAccount(user.getId(), user.getLoginId(), user.getName(), role.getId());
    }

    /**
     * Access Token 인증 실패에 사용할 프레임워크 독립 예외를 생성한다.
     *
     * @return 유효하지 않은 토큰 예외
     */
    private BusinessException invalid() {
        return new BusinessException(ErrorCode.TOKEN_INVALID, "유효하지 않은 액세스 토큰입니다.");
    }
}
