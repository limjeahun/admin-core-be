package com.espay.admincore.domain.model.history;

import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 비밀번호 로그인과 OTP 단계에서 발생한 인증 감사 이력 Aggregate Root.
 */
@Getter
public final class LoginHistory {
    /** 영속화 이후 부여되는 인증 이력 식별자이며 신규 이력에서는 {@code null}이다. */
    private final String id;
    /** 인증 과정에서 식별된 사용자 ID이며 로그인 ID 식별 전 실패하면 {@code null}이다. */
    private final String userId;
    /** 조회 시 사용자 테이블에서 조합하는 표시용 사용자명이며 저장 시에는 {@code null}이다. */
    private final String userName;
    /** 조회 시 사용자 테이블에서 조합하는 로그인 ID이며 저장 시에는 {@code null}이다. */
    private final String loginId;
    /** 비밀번호 로그인 또는 OTP 중 인증이 수행된 단계를 나타내는 {@code LOGIN}/{@code OTP} 값이다. */
    private final String authStep;
    /** 해당 인증 단계가 성공했는지 나타내는 결과다. */
    private final boolean success;
    /** 사용자가 입력한 관리자 시스템 접속 사유다. */
    private final String loginReason;
    /** 인증 실패 사유 코드이며 성공한 이력에서는 {@code null}이다. */
    private final String failReason;
    /** 로그인 요청에서 사용자가 실제로 입력한 로그인 ID다. */
    private final String inputId;
    /** 인증 요청을 보낸 클라이언트 IP 주소다. */
    private final String clientIp;
    /** 인증 요청에 포함된 클라이언트 User-Agent 값이다. */
    private final String userAgent;
    /** 인증 시도가 발생한 시각이다. */
    private final LocalDateTime createdAt;

    /**
     * 신규 생성과 영속·조회 상태 복원에 공통으로 사용하는 내부 생성자다.
     *
     * @param id 영속화된 이력 ID, 신규 이력이면 {@code null}
     * @param userId 인증 과정에서 식별된 사용자 ID
     * @param userName 조회 시 조합한 사용자명
     * @param loginId 조회 시 조합한 로그인 ID
     * @param authStep LOGIN 또는 OTP 인증 단계
     * @param success 인증 성공 여부
     * @param loginReason 관리자 시스템 접속 사유
     * @param failReason 인증 실패 사유 코드, 성공한 경우 {@code null}
     * @param inputId 사용자가 실제 입력한 로그인 ID
     * @param clientIp 요청 클라이언트 IP
     * @param userAgent 요청 클라이언트 User-Agent
     * @param createdAt 인증 시도 시각
     */
    private LoginHistory(String id, String userId, String userName, String loginId, String authStep,
                         boolean success, String loginReason, String failReason, String inputId,
                         String clientIp, String userAgent, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.loginId = loginId;
        this.authStep = authStep;
        this.success = success;
        this.loginReason = loginReason;
        this.failReason = failReason;
        this.inputId = inputId;
        this.clientIp = clientIp;
        this.userAgent = userAgent;
        this.createdAt = createdAt;
    }

    /**
     * 비밀번호 로그인이 성공한 신규 감사 이력을 생성한다.
     *
     * @param userId 인증에 성공한 사용자 ID
     * @param loginReason 사용자가 입력한 관리자 시스템 접속 사유
     * @param inputId 사용자가 실제 입력한 로그인 ID
     * @param clientIp 요청 클라이언트 IP
     * @param userAgent 요청 클라이언트 User-Agent
     * @return LOGIN 성공 상태와 현재 시각을 가진 신규 이력
     */
    public static LoginHistory loginSucceeded(String userId, String loginReason, String inputId,
                                              String clientIp, String userAgent) {
        return create(userId, "LOGIN", true, loginReason, null, inputId, clientIp, userAgent);
    }

    /**
     * 사용자 식별 또는 비밀번호 검증에 실패한 신규 감사 이력을 생성한다.
     *
     * @param loginReason 사용자가 입력한 관리자 시스템 접속 사유
     * @param failReason 인증 실패 사유 코드
     * @param inputId 사용자가 실제 입력한 로그인 ID
     * @param clientIp 요청 클라이언트 IP
     * @param userAgent 요청 클라이언트 User-Agent
     * @return 사용자 ID가 없고 LOGIN 실패 상태인 신규 이력
     */
    public static LoginHistory loginFailed(String loginReason, String failReason, String inputId,
                                           String clientIp, String userAgent) {
        return create(null, "LOGIN", false, loginReason, failReason, inputId, clientIp, userAgent);
    }

    /**
     * OTP 인증이 성공한 신규 감사 이력을 생성한다.
     *
     * @param userId OTP 인증에 성공한 사용자 ID
     * @param loginReason 비밀번호 로그인 단계에서 전달된 접속 사유
     * @param inputId 인증 대상 사용자의 로그인 ID
     * @param clientIp 요청 클라이언트 IP
     * @param userAgent 요청 클라이언트 User-Agent
     * @return OTP 성공 상태와 현재 시각을 가진 신규 이력
     */
    public static LoginHistory otpSucceeded(String userId, String loginReason, String inputId,
                                            String clientIp, String userAgent) {
        return create(userId, "OTP", true, loginReason, null, inputId, clientIp, userAgent);
    }

    /**
     * OTP 검증에 실패한 신규 감사 이력을 생성한다.
     *
     * @param userId OTP 인증 대상 사용자 ID
     * @param loginReason 비밀번호 로그인 단계에서 전달된 접속 사유
     * @param failReason OTP 불일치 또는 허용 횟수 초과 사유 코드
     * @param inputId 인증 대상 사용자의 로그인 ID
     * @param clientIp 요청 클라이언트 IP
     * @param userAgent 요청 클라이언트 User-Agent
     * @return OTP 실패 상태와 현재 시각을 가진 신규 이력
     */
    public static LoginHistory otpFailed(String userId, String loginReason, String failReason, String inputId,
                                         String clientIp, String userAgent) {
        return create(userId, "OTP", false, loginReason, failReason, inputId, clientIp, userAgent);
    }

    /**
     * 인증 단계와 결과가 확정된 신규 이력을 생성하는 내부 팩토리다.
     *
     * @param userId 인증 과정에서 식별된 사용자 ID
     * @param authStep LOGIN 또는 OTP 인증 단계
     * @param success 인증 성공 여부
     * @param loginReason 관리자 시스템 접속 사유
     * @param failReason 인증 실패 사유 코드
     * @param inputId 사용자가 실제 입력한 로그인 ID
     * @param clientIp 요청 클라이언트 IP
     * @param userAgent 요청 클라이언트 User-Agent
     * @return 아직 ID가 없는 현재 시각의 인증 이력
     */
    private static LoginHistory create(String userId, String authStep, boolean success, String loginReason,
                                       String failReason, String inputId, String clientIp, String userAgent) {
        return new LoginHistory(null, userId, null, null, authStep, success, loginReason, failReason,
                inputId, clientIp, userAgent, LocalDateTime.now());
    }

    /**
     * 영속성 또는 조회 결과의 기존 로그인 이력을 Aggregate로 복원한다.
     *
     * @param id 영속화된 이력 ID
     * @param userId 인증 과정에서 식별된 사용자 ID
     * @param userName 조회 시 사용자 테이블에서 조합한 사용자명
     * @param loginId 조회 시 사용자 테이블에서 조합한 로그인 ID
     * @param authStep LOGIN 또는 OTP 인증 단계
     * @param success 인증 성공 여부
     * @param loginReason 관리자 시스템 접속 사유
     * @param failReason 인증 실패 사유 코드
     * @param inputId 사용자가 실제 입력한 로그인 ID
     * @param clientIp 요청 클라이언트 IP
     * @param userAgent 요청 클라이언트 User-Agent
     * @param createdAt 인증 시도 시각
     * @return 영속 또는 조회 상태로 복원된 로그인 이력 Aggregate
     */
    public static LoginHistory reconstitute(String id, String userId, String userName, String loginId,
                                            String authStep, boolean success, String loginReason, String failReason,
                                            String inputId, String clientIp, String userAgent,
                                            LocalDateTime createdAt) {
        return new LoginHistory(id, userId, userName, loginId, authStep, success, loginReason, failReason,
                inputId, clientIp, userAgent, createdAt);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        return object instanceof LoginHistory other && id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
