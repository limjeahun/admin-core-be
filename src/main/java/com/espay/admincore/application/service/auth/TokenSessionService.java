package com.espay.admincore.application.service.auth;

import com.espay.admincore.application.dto.auth.AccessTokenSubject;
import com.espay.admincore.application.dto.auth.LoginResult;
import com.espay.admincore.application.port.in.auth.LogoutUseCase;
import com.espay.admincore.application.port.in.auth.RefreshTokenUseCase;
import com.espay.admincore.application.port.out.auth.JwtTokenPort;
import com.espay.admincore.application.port.out.auth.RefreshTokenPort;
import com.espay.admincore.application.port.out.role.RolePersistencePort;
import com.espay.admincore.application.port.out.user.UserLookupPort;
import com.espay.admincore.application.exception.BusinessException;
import com.espay.admincore.application.exception.ErrorCode;
import com.espay.admincore.domain.exception.UserNotFoundException;
import com.espay.admincore.domain.model.role.AdminRole;
import com.espay.admincore.domain.model.user.AdminUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Refresh Token 회전과 로그아웃 상태를 관리하는 서비스.
 */
@Service
@RequiredArgsConstructor
public class TokenSessionService implements RefreshTokenUseCase, LogoutUseCase {
    private final JwtTokenPort jwtTokenPort;
    private final RefreshTokenPort refreshTokenPort;
    private final UserLookupPort userLookupPort;
    private final RolePersistencePort rolePersistencePort;

    /**
     * Refresh JWT와 Redis 저장값을 검증하고 현재 사용자·권한 기준의 새 토큰 쌍을 발급한다.
     *
     * @param refreshToken 클라이언트가 제시한 Refresh Token
     * @return 회전된 토큰과 현재 사용자·권한 정보
     */
    @Override
    public LoginResult refreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID, "리프레시 토큰이 없습니다.");
        }
        if (jwtTokenPort.isExpired(refreshToken)) {
            throw new BusinessException(ErrorCode.TOKEN_EXPIRED, "리프레시 토큰이 만료되었습니다.");
        }
        if (!jwtTokenPort.isValid(refreshToken, "REFRESH")) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID, "유효하지 않은 리프레시 토큰입니다.");
        }
        String userId = jwtTokenPort.getUserId(refreshToken);
        AdminUser user = userLookupPort.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        AdminRole role = validate(user);
        return rotateTokens(refreshToken, user, role);
    }

    /**
     * 쿠키의 Refresh Token이 유효하고 서버 저장값과 일치하면 삭제한다.
     * 누락되거나 이미 무효한 토큰은 쿠키 삭제만으로 로그아웃을 마칠 수 있게 무시한다.
     *
     * @param refreshToken 로그아웃 요청 쿠키에 담긴 Refresh Token
     */
    @Override
    public void logout(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()
                || !jwtTokenPort.isValid(refreshToken, "REFRESH")) {
            return;
        }
        String userId = jwtTokenPort.getUserId(refreshToken);
        refreshTokenPort.deleteIfMatches(userId, refreshToken);
    }

    /**
     * 현재 사용자·권한으로 새 토큰을 만들고 기존 Refresh Token과 원자적으로 교체한다.
     *
     * @param currentRefreshToken 클라이언트가 제시한 기존 Refresh Token
     * @param user 토큰 주체 사용자
     * @param role 사용자의 현재 활성 권한
     * @return 토큰과 사용자·권한 정보
     */
    private LoginResult rotateTokens(String currentRefreshToken, AdminUser user, AdminRole role) {
        AccessTokenSubject subject = new AccessTokenSubject(user.getId(), user.getLoginId(), role.getId());
        String accessToken = jwtTokenPort.issueAccessToken(subject);
        String newRefreshToken = jwtTokenPort.issueRefreshToken(user.getId());
        long refreshTokenExpiresInMillis = jwtTokenPort.refreshTokenExpiresInMillis();
        boolean rotated = refreshTokenPort.rotate(
                user.getId(), currentRefreshToken, newRefreshToken, refreshTokenExpiresInMillis);
        if (!rotated) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID, "리프레시 토큰이 일치하지 않습니다.");
        }
        return new LoginResult(
                accessToken,
                newRefreshToken,
                "Bearer",
                jwtTokenPort.accessTokenExpiresInSeconds(),
                refreshTokenExpiresInMillis,
                user.getId(),
                user.getName(),
                role.getId(),
                role.getName()
        );
    }

    /**
     * 토큰 갱신 전에 사용자와 권한의 현재 활성 상태를 확인한다.
     *
     * @param user Refresh Token의 사용자
     * @return 활성 권한
     */
    private AdminRole validate(AdminUser user) {
        user.validateLoginAllowed();
        AdminRole role = rolePersistencePort.findById(user.getRoleId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ACCOUNT_LOCKED, "사용자 권한을 확인할 수 없습니다."));
        role.validateLoginAllowed();
        return role;
    }
}
