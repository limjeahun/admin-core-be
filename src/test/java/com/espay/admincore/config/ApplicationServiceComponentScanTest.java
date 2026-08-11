package com.espay.admincore.config;

import com.espay.admincore.application.port.in.auth.AccessTokenAuthenticationUseCase;
import com.espay.admincore.application.port.in.auth.LoginUseCase;
import com.espay.admincore.application.port.in.auth.OtpVerifyUseCase;
import com.espay.admincore.application.port.in.menu.MenuQueryUseCase;
import com.espay.admincore.application.port.in.menu.RoleMenuUseCase;
import com.espay.admincore.application.port.in.user.UserCommandUseCase;
import com.espay.admincore.application.port.out.auth.*;
import com.espay.admincore.application.port.out.file.ExcelDocumentWriterPort;
import com.espay.admincore.application.port.out.history.FileHistoryPersistencePort;
import com.espay.admincore.application.port.out.history.LoginHistoryPersistencePort;
import com.espay.admincore.application.port.out.menu.MenuCatalogPort;
import com.espay.admincore.application.port.out.menu.RoleMenuPersistencePort;
import com.espay.admincore.application.port.out.role.RolePersistencePort;
import com.espay.admincore.application.port.out.user.UserLookupPort;
import com.espay.admincore.application.port.out.user.UserPersistencePort;
import com.espay.admincore.application.port.out.user.UserSearchPort;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Component Scan이 Application Service를 수동 조립 없이 UseCase Bean으로 등록하는지 검증한다.
 */
class ApplicationServiceComponentScanTest {

    /**
     * 출력 Port와 OTP 설정만 제공하면 모든 핵심 UseCase가 자동 등록되는지 확인한다.
     */
    @Test
    void registersApplicationServicesThroughComponentScan() {
        new ApplicationContextRunner()
                .withUserConfiguration(ServiceScanConfiguration.class)
                .withBean(FileHistoryPersistencePort.class, () -> mock(FileHistoryPersistencePort.class))
                .withBean(LoginHistoryPersistencePort.class, () -> mock(LoginHistoryPersistencePort.class))
                .withBean(ExcelDocumentWriterPort.class, () -> mock(ExcelDocumentWriterPort.class))
                .withBean(UserLookupPort.class, () -> mock(UserLookupPort.class))
                .withBean(UserPersistencePort.class, () -> mock(UserPersistencePort.class))
                .withBean(UserSearchPort.class, () -> mock(UserSearchPort.class))
                .withBean(RolePersistencePort.class, () -> mock(RolePersistencePort.class))
                .withBean(MenuCatalogPort.class, () -> mock(MenuCatalogPort.class))
                .withBean(RoleMenuPersistencePort.class, () -> mock(RoleMenuPersistencePort.class))
                .withBean(PasswordHashPort.class, () -> mock(PasswordHashPort.class))
                .withBean(PreAuthTokenPort.class, () -> mock(PreAuthTokenPort.class))
                .withBean(PreAuthStatePort.class, () -> mock(PreAuthStatePort.class))
                .withBean(JwtTokenPort.class, () -> mock(JwtTokenPort.class))
                .withBean(RefreshTokenPort.class, () -> mock(RefreshTokenPort.class))
                .withBean(OtpSecretGeneratorPort.class, () -> mock(OtpSecretGeneratorPort.class))
                .withBean(OtpQrUriFactoryPort.class, () -> mock(OtpQrUriFactoryPort.class))
                .withBean(OtpQrCodeGeneratorPort.class, () -> mock(OtpQrCodeGeneratorPort.class))
                .withBean(OtpMailPort.class, () -> mock(OtpMailPort.class))
                .withBean(OtpCodeVerifierPort.class, () -> mock(OtpCodeVerifierPort.class))
                .withBean(OtpProperties.class,
                        () -> new OtpProperties("ADMIN", "ESPAY", 240, 5))
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).hasSingleBean(LoginUseCase.class);
                    assertThat(context).hasSingleBean(AccessTokenAuthenticationUseCase.class);
                    assertThat(context).hasSingleBean(OtpVerifyUseCase.class);
                    assertThat(context).hasSingleBean(MenuQueryUseCase.class);
                    assertThat(context).hasSingleBean(RoleMenuUseCase.class);
                    assertThat(context).hasSingleBean(UserCommandUseCase.class);
                });
    }

    /**
     * Application Service 패키지만 Component Scan하고 OTP 정책 설정을 불러오는 테스트 구성.
     */
    @Configuration(proxyBeanMethods = false)
    @ComponentScan("com.espay.admincore.application.service")
    @Import(OtpConfig.class)
    static class ServiceScanConfiguration {
    }
}
