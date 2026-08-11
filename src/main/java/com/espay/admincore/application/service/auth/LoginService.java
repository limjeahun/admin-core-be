package com.espay.admincore.application.service.auth;

import com.espay.admincore.application.dto.auth.*;
import com.espay.admincore.application.port.in.auth.LoginUseCase;
import com.espay.admincore.application.port.out.auth.PasswordHashPort;
import com.espay.admincore.application.port.out.auth.PreAuthStatePort;
import com.espay.admincore.application.port.out.auth.PreAuthTokenPort;
import com.espay.admincore.application.port.out.history.LoginHistoryPersistencePort;
import com.espay.admincore.application.port.out.role.RolePersistencePort;
import com.espay.admincore.application.port.out.user.UserLookupPort;
import com.espay.admincore.application.exception.BusinessException;
import com.espay.admincore.application.exception.ErrorCode;
import com.espay.admincore.domain.exception.InactiveRoleException;
import com.espay.admincore.domain.exception.InactiveUserException;
import com.espay.admincore.domain.model.history.LoginHistory;
import com.espay.admincore.domain.model.role.AdminRole;
import com.espay.admincore.domain.model.user.AdminUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 아이디·비밀번호를 검증하고 OTP 단계용 사전 인증 상태를 생성하는 로그인 서비스.
 */
@Service
@RequiredArgsConstructor
public class LoginService implements LoginUseCase {
    private final UserLookupPort                userLookupPort;
    private final RolePersistencePort           rolePersistencePort;
    private final PasswordHashPort              passwordHashPort;
    private final PreAuthTokenPort              preAuthTokenPort;
    private final PreAuthStatePort              preAuthStatePort;
    private final LoginHistoryPersistencePort   loginHistoryPersistencePort;

    /**
     * 사용자·권한 도메인 규칙과 비밀번호 해시 포트로 자격 증명을 검증하고 OTP 로그인 챌린지를 발급한다.
     *
     * <p>성공과 자격 증명 실패는 각각 LOGIN 감사 이력으로 별도 저장한다.</p>
     *
     * @param command 로그인 자격 증명, 접속 사유와 클라이언트 정보
     * @return 사전 인증 토큰과 OTP 등록 여부
     */
    @Override
    @Transactional
    public LoginChallengeResult login(LoginCommand command) {
        try {
            // 1. 로그인 ID로 사용자 도메인 모델을 한 번만 조회한다.
            AdminUser user = userLookupPort.findByLoginId(command.loginId())
                    .orElseThrow(this::invalidCredentials);
            // 2. 사용자와 연결 권한의 도메인 규칙으로 로그인 가능 상태를 확인한다.
            validateLoginAllowed(user);
            // 3. 평문 비밀번호 비교는 애플리케이션이 선언한 출력 포트에 위임한다.
            if (!passwordHashPort.matches(command.password(), user.getPasswordHash())) {
                throw invalidCredentials();
            }
            // 4. OTP 인증 단계에서 사용할 단기 사전 인증 JWT를 발급한다.
            IssuedPreAuthToken token = preAuthTokenPort.issue(user.getId(), PreAuthPurpose.OTP_LOGIN);
            // 5. 토큰 재사용과 실패 횟수를 관리할 서버 측 사전 인증 상태를 저장한다.
            preAuthStatePort.create(
                    CreatePreAuthStateCommand.of(
                            token.jti(), user.getId(), PreAuthPurpose.OTP_LOGIN, token.expiresInSeconds()
                    )
            );
            // 6. 아이디·비밀번호 인증 성공 이력을 기록한다.
            loginHistoryPersistencePort.save(
                    LoginHistory.loginSucceeded(
                            user.getId(),
                            command.loginReason(),
                            command.loginId(),
                            command.clientIp(),
                            command.userAgent()
                    )
            );
            // 7. 클라이언트가 OTP 인증을 이어갈 수 있도록 챌린지 정보를 반환한다.
            return new LoginChallengeResult(
                    token.token(),
                    token.expiresInSeconds(),
                    user.getLoginId(),
                    user.getName(),
                    user.hasOtpSecret()
            );
        } catch (BusinessException | InactiveUserException | InactiveRoleException exception) {
            // 예상 가능한 인증 실패를 별도 트랜잭션으로 기록한 뒤 입력 어댑터까지 다시 전파한다.
            loginHistoryPersistencePort.save(
                    LoginHistory.loginFailed(
                            command.loginReason(),
                            failReason(exception),
                            command.loginId(),
                            command.clientIp(),
                            command.userAgent()
                    )
            );
            throw exception;
        }
    }

    /**
     * 사용자가 활성 상태이고 존재하는 활성 권한을 보유했는지 확인한다.
     *
     * @param user 비밀번호 검증을 통과한 사용자
     * @throws BusinessException 연결된 권한을 찾을 수 없는 경우
     * @throws InactiveUserException 사용자가 비활성 상태인 경우
     * @throws InactiveRoleException 권한이 비활성 상태인 경우
     */
    private void validateLoginAllowed(AdminUser user) {
        // 1. 사용자 Aggregate가 자신의 활성 상태 규칙을 검증한다.
        user.validateLoginAllowed();
        // 2. 사용자에게 연결된 권한이 실제로 존재하는지 확인한다.
        AdminRole role = rolePersistencePort.findById(user.getRoleId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ACCOUNT_LOCKED, "사용자 권한을 확인할 수 없습니다."));
        // 3. Role Aggregate가 자신의 활성 상태 규칙을 검증한다.
        role.validateLoginAllowed();
    }

    /**
     * 사용자 존재 여부를 노출하지 않는 공통 자격 증명 실패 예외를 생성한다.
     *
     * @return 아이디 또는 비밀번호 불일치 예외
     */
    private BusinessException invalidCredentials() {
        return new BusinessException(ErrorCode.INVALID_CREDENTIALS, "아이디 또는 비밀번호를 확인해 주세요.");
    }

    /**
     * 감사 이력에 저장할 실패 사유를 프레임워크 독립 예외 기준으로 결정한다.
     *
     * @param exception 로그인 과정에서 발생한 예상 가능한 예외
     * @return 자격 증명 불일치 또는 계정 잠금 코드
     */
    private String failReason(RuntimeException exception) {
        if (exception instanceof BusinessException businessException) {
            return businessException.getErrorCode().getCode();
        }
        return ErrorCode.ACCOUNT_LOCKED.getCode();
    }
}
