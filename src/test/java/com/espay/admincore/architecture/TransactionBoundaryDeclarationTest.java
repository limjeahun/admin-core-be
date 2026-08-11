package com.espay.admincore.architecture;

import com.espay.admincore.application.dto.auth.OtpVerifyCommand;
import com.espay.admincore.application.dto.history.DownloadHistoryExcelCommand;
import com.espay.admincore.application.dto.user.DownloadUsersExcelCommand;
import com.espay.admincore.application.dto.role.CreateRoleCommand;
import com.espay.admincore.application.service.auth.OtpVerificationService;
import com.espay.admincore.application.service.auth.TokenSessionService;
import com.espay.admincore.application.service.history.HistoryExportService;
import com.espay.admincore.application.service.role.RoleService;
import com.espay.admincore.application.service.user.UserExportService;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 주요 유스케이스의 트랜잭션 경계가 서비스 코드에 명시되어 있는지 확인한다.
 */
class TransactionBoundaryDeclarationTest {

    /**
     * 감사 이력을 저장하는 내보내기 서비스가 조회 전용으로 선언되지 않았는지 확인한다.
     */
    @Test
    void exportServicesUseWriteTransactions() throws NoSuchMethodException {
        assertThat(transactional(HistoryExportService.class,
                "downloadLoginHistoryExcel", DownloadHistoryExcelCommand.class).readOnly()).isFalse();
        assertThat(transactional(HistoryExportService.class,
                "downloadFileHistoryExcel", DownloadHistoryExcelCommand.class).readOnly()).isFalse();
        assertThat(transactional(UserExportService.class,
                "downloadUsersExcel", DownloadUsersExcelCommand.class).readOnly()).isFalse();
    }

    /**
     * OTP 완료와 역할 생성의 쓰기 트랜잭션이 실제 서비스에 선언되어 있는지 확인한다.
     *
     * @throws NoSuchMethodException 역할 생성 메서드를 찾지 못한 경우
     */
    @Test
    void writeUseCasesDeclareTransactionsDirectly() throws NoSuchMethodException {
        assertThat(transactional(OtpVerificationService.class,
                "verifyOtp", OtpVerifyCommand.class).readOnly()).isFalse();
        assertThat(transactional(RoleService.class,
                "createRole", CreateRoleCommand.class).readOnly()).isFalse();
    }

    /**
     * Redis 상태만 변경하는 토큰 세션 서비스에 불필요한 JPA 트랜잭션이 없는지 확인한다.
     */
    @Test
    void redisSessionServiceDoesNotDeclareJpaTransaction() {
        assertThat(TokenSessionService.class.getAnnotation(Transactional.class)).isNull();
    }

    /**
     * 서비스 메서드에 선언된 트랜잭션 애노테이션을 반환한다.
     *
     * @param serviceType 검사할 서비스 클래스
     * @param methodName 검사할 메서드명
     * @param parameterTypes 메서드 파라미터 타입
     * @return 메서드에 선언된 트랜잭션 정보
     * @throws NoSuchMethodException 검사할 메서드를 찾지 못한 경우
     */
    private Transactional transactional(Class<?> serviceType, String methodName, Class<?>... parameterTypes)
            throws NoSuchMethodException {
        Transactional transactional = serviceType.getMethod(methodName, parameterTypes)
                .getAnnotation(Transactional.class);
        assertThat(transactional).as(serviceType.getSimpleName() + "#" + methodName).isNotNull();
        return transactional;
    }
}
