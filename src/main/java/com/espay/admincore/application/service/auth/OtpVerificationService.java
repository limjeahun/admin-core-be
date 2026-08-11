package com.espay.admincore.application.service.auth;

import com.espay.admincore.application.dto.auth.*;
import com.espay.admincore.application.dto.history.FindLatestLoginReasonQuery;
import com.espay.admincore.application.port.in.auth.OtpVerifyUseCase;
import com.espay.admincore.application.port.out.auth.JwtTokenPort;
import com.espay.admincore.application.port.out.auth.OtpCodeVerifierPort;
import com.espay.admincore.application.port.out.auth.PreAuthStatePort;
import com.espay.admincore.application.port.out.auth.PreAuthTokenPort;
import com.espay.admincore.application.port.out.auth.RefreshTokenPort;
import com.espay.admincore.application.port.out.history.LoginHistoryPersistencePort;
import com.espay.admincore.application.port.out.role.RolePersistencePort;
import com.espay.admincore.application.port.out.user.UserLookupPort;
import com.espay.admincore.application.port.out.user.UserPersistencePort;
import com.espay.admincore.application.exception.BusinessException;
import com.espay.admincore.application.exception.ErrorCode;
import com.espay.admincore.domain.model.history.LoginHistory;
import com.espay.admincore.domain.model.role.AdminRole;
import com.espay.admincore.domain.model.user.AdminUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * TOTP를 검증하고 일회성 사전 인증 상태를 소비해 최종 로그인을 완료하는 서비스.
 */
@Service
@RequiredArgsConstructor
public class OtpVerificationService implements OtpVerifyUseCase {
    private final PreAuthTokenPort              preAuthTokenPort;
    private final PreAuthStatePort              preAuthStatePort;
    private final UserLookupPort                userLookupPort;
    private final RolePersistencePort           rolePersistencePort;
    private final OtpCodeVerifierPort           otpCodeVerifierPort;
    private final LoginHistoryPersistencePort   loginHistoryPersistencePort;
    private final JwtTokenPort                   jwtTokenPort;
    private final RefreshTokenPort               refreshTokenPort;
    private final UserPersistencePort            userPersistencePort;
    private final OtpPolicy                     otpPolicy;

    /**
     * 사전 인증과 OTP를 검증하고 성공 시 토큰을 발급하며 성공·실패 감사 이력을 기록한다.
     *
     * <p>OTP 실패 횟수가 설정값에 도달하면 Redis 사전 인증 상태가 제거된다.</p>
     *
     * @param command 사전 인증 토큰, OTP 번호와 요청 감사 정보
     * @return Access/Refresh Token과 사용자·권한 정보
     */
    @Override
    @Transactional
    public LoginResult verifyOtp(OtpVerifyCommand command) {
        // 1. 사전 인증 토큰과 Redis 인증 상태를 검증한다.
        PreAuthClaims claims = validatePreAuth(command.preAuthToken());
        // 2. 사용자와 권한이 현재 로그인 가능한 상태인지 확인한다.
        AdminUser user = userLookupPort.findById(claims.userId())
                .orElseThrow(this::invalid);
        AdminRole role = validateLoginAllowed(user);
        // 3. 로그인 단계에서 기록한 접속 사유를 OTP 이력에 연결한다.
        String loginReason = loginHistoryPersistencePort.findLatestLoginReason(
                FindLatestLoginReasonQuery.of(user.getId(), user.getLoginId(), command.clientIp())
        );
        // 4. OTP 검증 후 사전 인증 상태를 소비해 토큰 재사용을 막는다.
        validateOtp(user, claims, command, loginReason);
        consumePreAuthState(claims, user.getId());
        // 5. 최종 로그인 토큰을 발급하고 OTP 성공 이력을 저장한다.
        LoginResult result = issueTokens(user, role);
        userPersistencePort.save(user.updateLastLoginAt(LocalDateTime.now()));
        loginHistoryPersistencePort.save(
                LoginHistory.otpSucceeded(
                        user.getId(), loginReason, user.getLoginId(), command.clientIp(), command.userAgent()
                )
        );
        return result;
    }

    /**
     * OTP 인증을 완료한 사용자에게 토큰을 발급하고 Refresh Token 상태를 저장한다.
     *
     * @param user 토큰을 발급할 사용자
     * @param role 사용자의 현재 활성 권한
     * @return 발급된 토큰과 사용자·권한 정보
     */
    private LoginResult issueTokens(AdminUser user, AdminRole role) {
        AccessTokenSubject subject = new AccessTokenSubject(user.getId(), user.getLoginId(), role.getId());
        String accessToken = jwtTokenPort.issueAccessToken(subject);
        String refreshToken = jwtTokenPort.issueRefreshToken(user.getId());
        refreshTokenPort.save(SaveRefreshTokenCommand.of(
                user.getId(), refreshToken, jwtTokenPort.refreshTokenExpiresInMillis())
        );
        return new LoginResult(
                accessToken,
                refreshToken,
                "Bearer",
                jwtTokenPort.accessTokenExpiresInSeconds(),
                jwtTokenPort.refreshTokenExpiresInMillis(),
                user.getId(),
                user.getName(),
                role.getId(),
                role.getName()
        );
    }

    /**
     * 사전 인증 JWT와 Redis 상태의 토큰 ID, 사용자와 목적을 교차 검증한다.
     *
     * @param token 검증할 사전 인증 토큰
     * @return 검증된 클레임
     */
    private PreAuthClaims validatePreAuth(String token) {
        PreAuthClaims claims = preAuthTokenPort.verify(token);
        PreAuthState state = preAuthStatePort.find(claims.jti()).orElseThrow(this::invalid);
        if (claims.purpose() != PreAuthPurpose.OTP_LOGIN || !claims.userId().equals(state.userId())
                || claims.purpose() != state.purpose()) {
            throw invalid();
        }
        return claims;
    }

    /**
     * 등록된 OTP 번호를 검증하고 실패 횟수와 감사 이력을 기록한다.
     *
     * @param user OTP 인증 대상 사용자
     * @param claims 검증된 사전 인증 클레임
     * @param command 입력한 OTP 번호와 요청 정보
     * @param loginReason 로그인 단계에서 기록한 접속 사유
     * @throws BusinessException OTP가 등록되지 않았거나 입력 번호가 일치하지 않는 경우
     */
    private void validateOtp(AdminUser user, PreAuthClaims claims, OtpVerifyCommand command, String loginReason) {
        if (user.hasOtpSecret() && otpCodeVerifierPort.verify(user.getOtpSecret(), command.otpNumber())) {
            return;
        }

        int attempts = preAuthStatePort.recordFailure(claims.jti(), otpPolicy.maxFailedAttempts());
        String failReason = attempts >= otpPolicy.maxFailedAttempts()
                ? "OTP_ATTEMPTS_EXCEEDED"
                : "INVALID_OTP";

        loginHistoryPersistencePort.save(LoginHistory.otpFailed(
                user.getId(), loginReason, failReason,
                user.getLoginId(), command.clientIp(), command.userAgent()));
        throw invalid();
    }

    /**
     * 사전 인증 상태를 원자적으로 소비해 동일한 토큰의 재사용을 막는다.
     *
     * @param claims 검증된 사전 인증 클레임
     * @param userId OTP 인증 대상 사용자 ID
     * @throws BusinessException 인증 상태가 만료됐거나 이미 소비된 경우
     */
    private void consumePreAuthState(PreAuthClaims claims, String userId) {
        boolean consumed = preAuthStatePort.consume(
                ConsumePreAuthStateCommand.of(claims.jti(), userId, PreAuthPurpose.OTP_LOGIN));
        if (!consumed) {
            throw invalid();
        }
    }

    /**
     * 최종 로그인 직전에 사용자와 권한의 현재 활성 상태를 다시 확인한다.
     *
     * @param user OTP 인증 대상 사용자
     * @return 활성 권한
     */
    private AdminRole validateLoginAllowed(AdminUser user) {
        user.validateLoginAllowed();
        AdminRole role = rolePersistencePort.findById(user.getRoleId()).orElseThrow(this::invalid);
        role.validateLoginAllowed();
        return role;
    }

    /**
     * OTP 또는 사전 인증 실패에 사용할 401 비즈니스 예외를 생성한다.
     *
     * @return 인증 정보 만료 또는 불일치 예외
     */
    private BusinessException invalid() {
        return new BusinessException(ErrorCode.UNAUTHORIZED, "인증 정보가 유효하지 않거나 만료되었습니다.");
    }
}
