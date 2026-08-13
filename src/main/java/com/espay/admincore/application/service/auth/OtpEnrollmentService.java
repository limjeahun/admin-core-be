package com.espay.admincore.application.service.auth;

import com.espay.admincore.application.dto.auth.*;
import com.espay.admincore.application.port.in.auth.OtpBarcodeIssueUseCase;
import com.espay.admincore.application.port.out.auth.*;
import com.espay.admincore.application.port.out.role.RolePersistencePort;
import com.espay.admincore.application.port.out.user.UserLookupPort;
import com.espay.admincore.application.port.out.user.UserPersistencePort;
import com.espay.admincore.application.exception.BusinessException;
import com.espay.admincore.application.exception.ErrorCode;
import com.espay.admincore.domain.exception.InactiveRoleException;
import com.espay.admincore.domain.exception.InactiveUserException;
import com.espay.admincore.domain.model.user.AdminUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 사전 인증 사용자의 TOTP 비밀키와 QR 코드를 생성해 이메일로 등록 안내를 보내는 서비스.
 */
@Service
@RequiredArgsConstructor
public class OtpEnrollmentService implements OtpBarcodeIssueUseCase {
    private final PreAuthTokenPort          preAuthTokenPort;
    private final PreAuthStatePort          preAuthStatePort;
    private final UserLookupPort            userLookupPort;
    private final UserPersistencePort       userPersistencePort;
    private final RolePersistencePort       rolePersistencePort;
    private final OtpSecretGeneratorPort    secretGeneratorPort;
    private final OtpQrUriFactoryPort       uriFactoryPort;
    private final OtpQrCodeGeneratorPort    qrCodeGeneratorPort;
    private final OtpMailPort               mailPort;
    private final OtpPolicy                 otpPolicy;

    /**
     * 사전 인증과 계정 상태를 검증하고 새 OTP 비밀키를 메일 발송 성공 후 사용자에게 저장한다.
     *
     * @param command 사전 인증 토큰과 요청 클라이언트 정보
     */
    @Override
    @Transactional
    public void issueBarcode(OtpBarcodeIssueCommand command) {
        // 1. 사전 인증 토큰과 Redis 인증 상태가 일치하는지 확인한다.
        PreAuthClaims claims = validate(command.preAuthToken());
        // 2. OTP를 등록할 사용자와 권한이 현재 활성 상태인지 확인한다.
        AdminUser user = userLookupPort.findById(claims.userId())
                .orElseThrow(this::invalid);
        validateActive(user);
        // 3. 새로운 OTP 비밀키를 생성하고 인증 앱 등록에 사용할 QR 코드를 만든다.
        String secret = secretGeneratorPort.generate();
        String uri = uriFactoryPort.create(
                CreateOtpQrUriCommand.of(user.getLoginId(), otpPolicy.issuer(), secret)
        );
        String qrCode = qrCodeGeneratorPort.generateBase64(
                GenerateOtpQrCodeCommand.of(uri, otpPolicy.qrSize(), otpPolicy.qrSize())
        );
        // 4. QR 코드 메일 발송이 성공한 경우에만 새 비밀키를 사용자에게 저장한다.
        mailPort.sendOtpBarcodeEmail(
                SendOtpBarcodeEmailCommand.of(user.getEmail(), user.getName(), qrCode)
        );
        userPersistencePort.save(
                user.registerOtpSecret(secret)
        );
    }

    /**
     * 사전 인증 JWT와 서버 측 상태의 토큰 ID, 사용자 및 목적이 일치하는지 확인한다.
     *
     * @param token 검증할 사전 인증 JWT
     * @return 검증된 사전 인증 클레임
     * @throws BusinessException 토큰이 만료·변조되었거나 서버 상태와 일치하지 않는 경우
     */
    private PreAuthClaims validate(String token) {
        PreAuthClaims claims = preAuthTokenPort.verify(token);
        PreAuthState state = preAuthStatePort.find(claims.jti()).orElseThrow(this::invalid);
        if (claims.purpose() != PreAuthPurpose.OTP_LOGIN || !claims.userId().equals(state.userId())
                || claims.purpose() != state.purpose()) {
            throw invalid();
        }
        return claims;
    }

    /**
     * OTP를 등록할 사용자와 부여된 권한이 현재 활성 상태인지 확인한다.
     *
     * @param user OTP를 등록할 사용자
     * @throws BusinessException 사용자에게 연결된 권한을 찾을 수 없는 경우
     * @throws InactiveUserException 사용자가 비활성 상태인 경우
     * @throws InactiveRoleException 권한이 비활성 상태인 경우
     */
    private void validateActive(AdminUser user) {
        user.validateLoginAllowed();
        var role = rolePersistencePort.findById(user.getRoleId()).orElseThrow(this::invalid);
        role.validateLoginAllowed();
    }

    /**
     * 사전 인증 실패에 사용할 일관된 401 비즈니스 예외를 생성한다.
     *
     * @return 인증 정보 만료 또는 불일치 예외
     */
    private BusinessException invalid() {
        return new BusinessException(ErrorCode.UNAUTHORIZED, "인증 정보가 유효하지 않거나 만료되었습니다.");
    }
}
