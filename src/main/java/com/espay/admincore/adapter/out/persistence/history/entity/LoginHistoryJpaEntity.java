package com.espay.admincore.adapter.out.persistence.history.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * {@code login_logs} 테이블의 비밀번호 로그인·OTP 감사 이력 행을 표현하는 JPA 엔티티.
 */
@Getter
@Entity
@Table(name = "login_logs")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LoginHistoryJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "hist_id")
    private Long id;
    @Column(name = "user_id")
    private Long userId;
    @Column(name = "auth_step", nullable = false, length = 20)
    private String authStep;
    @Column(name = "result_yn", nullable = false, length = 1, columnDefinition = "char(1)")
    private String resultYn;
    @Column(name = "login_reason", length = 100)
    private String loginReason;
    @Column(name = "fail_reason", length = 255)
    private String failReason;
    @Column(name = "input_id", length = 100)
    private String inputId;
    @Column(name = "client_ip", length = 64)
    private String clientIp;
    @Column(name = "user_agent", length = 255)
    private String userAgent;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * 도메인 인증 이력을 영속화하거나 조회 결과를 복원하기 위한 전체 필드 생성자.
     *
     * @param id 로그인 이력 PK
     * @param userId 식별된 사용자 FK
     * @param authStep LOGIN 또는 OTP 단계
     * @param resultYn 성공 여부(Y/N)
     * @param loginReason 접속 사유
     * @param failReason 실패 사유
     * @param inputId 실제 입력 로그인 ID
     * @param clientIp 요청 IP
     * @param userAgent 요청 User-Agent
     * @param createdAt 인증 시도 시각
     */
    public LoginHistoryJpaEntity(Long id, Long userId, String authStep, String resultYn, String loginReason,
                                 String failReason, String inputId, String clientIp, String userAgent,
                                 LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.authStep = authStep;
        this.resultYn = resultYn;
        this.loginReason = loginReason;
        this.failReason = failReason;
        this.inputId = inputId;
        this.clientIp = clientIp;
        this.userAgent = userAgent;
        this.createdAt = createdAt;
    }
}
