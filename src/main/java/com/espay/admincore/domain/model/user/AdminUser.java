package com.espay.admincore.domain.model.user;

import com.espay.admincore.domain.exception.InactiveUserException;
import com.espay.admincore.domain.model.role.AdminRole;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 관리자 계정의 프로필, 인증 정보와 활성 상태를 표현하는 Aggregate Root.
 */
@Getter
public final class AdminUser {
    /** 영속화 이후 부여되는 사용자 식별자이며 신규 Aggregate에서는 {@code null}이다. */
    private final String id;
    /** 로그인 자격 증명 조회에 사용하는 중복 불가 로그인 ID다. */
    private final String loginId;
    /** 관리자 화면과 감사 이력에 표시할 사용자명이다. */
    private final String name;
    /** OTP 등록 안내와 사용자 식별에 사용하는 중복 불가 이메일 주소다. */
    private final String email;
    /** 관리자 연락에 사용하는 전화번호이며 등록하지 않을 수 있다. */
    private final String phoneNo;
    /** 사용자의 소속 또는 부서명이며 등록하지 않을 수 있다. */
    private final String deptName;
    /** 사용자에게 부여된 {@link AdminRole}의 식별자다. */
    private final String roleId;
    /** 비밀번호 검증에 사용하는 BCrypt 해시이며 원문 비밀번호를 저장하지 않는다. */
    private final String passwordHash;
    /** TOTP 생성과 검증에 사용하는 Base32 비밀키이며 OTP 등록 전에는 {@code null}이다. */
    private final String otpSecret;
    /** 비밀번호와 OTP 인증을 모두 완료한 마지막 로그인 시각이다. */
    private final LocalDateTime lastLoginAt;
    /** 로그인과 보호 API 사용 가능 여부를 결정하는 사용자 상태다. */
    private final UserStatus status;
    /** 사용자 계정이 최초 생성된 시각이며 수정 시에도 유지된다. */
    private final LocalDateTime createdAt;
    /** 프로필, 권한, 상태 또는 인증 정보가 마지막으로 변경된 시각이다. */
    private final LocalDateTime updatedAt;

    /**
     * 신규 생성과 영속 상태 복원에 공통으로 사용하는 내부 생성자다.
     *
     * @param id 영속화된 사용자 ID, 신규 사용자이면 {@code null}
     * @param loginId 중복될 수 없는 로그인 ID
     * @param name 사용자명
     * @param email OTP 안내에 사용할 이메일 주소
     * @param phoneNo 연락처, 등록하지 않은 경우 {@code null}
     * @param deptName 소속 또는 부서명, 등록하지 않은 경우 {@code null}
     * @param roleId 부여된 관리자 권한 ID
     * @param passwordHash BCrypt로 인코딩된 비밀번호
     * @param otpSecret TOTP 비밀키, 등록 전에는 {@code null}
     * @param lastLoginAt 최종 로그인 완료 시각, 로그인 전에는 {@code null}
     * @param status 사용자 활성 상태
     * @param createdAt 최초 생성 시각
     * @param updatedAt 최종 수정 시각
     */
    private AdminUser(String id, String loginId, String name, String email, String phoneNo, String deptName,
                      String roleId, String passwordHash, String otpSecret, LocalDateTime lastLoginAt,
                      UserStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.loginId = loginId;
        this.name = name;
        this.email = email;
        this.phoneNo = phoneNo;
        this.deptName = deptName;
        this.roleId = roleId;
        this.passwordHash = passwordHash;
        this.otpSecret = otpSecret;
        this.lastLoginAt = lastLoginAt;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 아직 ID가 없는 신규 관리자 계정을 생성한다.
     * OTP 비밀키와 최종 로그인 시각은 비어 있으며 상태를 생략하면 활성 상태로 생성한다.
     *
     * @param loginId 중복 검사를 마친 로그인 ID
     * @param name 사용자명
     * @param email 중복 검사를 마친 이메일 주소
     * @param phoneNo 연락처, 등록하지 않으면 {@code null}
     * @param deptName 소속 또는 부서명, 등록하지 않으면 {@code null}
     * @param roleId 존재하고 활성 상태임을 확인한 권한 ID
     * @param passwordHash Password Hash Port로 인코딩한 비밀번호
     * @param status 초기 사용자 상태, {@code null}이면 {@link UserStatus#ACTIVE}
     * @return 생성·수정 시각이 현재 시각으로 초기화된 신규 사용자
     */
    public static AdminUser create(String loginId, String name, String email, String phoneNo, String deptName,
                                   String roleId, String passwordHash, UserStatus status) {
        LocalDateTime now = LocalDateTime.now();
        return new AdminUser(null, loginId, name, email, phoneNo, deptName, roleId, passwordHash, null, null,
                status == null ? UserStatus.ACTIVE : status, now, now);
    }

    /**
     * 영속성에 저장된 기존 사용자 상태를 Aggregate로 복원한다.
     * 신규 생성 기본값이나 현재 시각을 적용하지 않고 저장된 상태를 그대로 사용한다.
     *
     * @param id 영속화된 사용자 ID
     * @param loginId 로그인 ID
     * @param name 사용자명
     * @param email 이메일 주소
     * @param phoneNo 연락처, 등록하지 않은 경우 {@code null}
     * @param deptName 소속 또는 부서명, 등록하지 않은 경우 {@code null}
     * @param roleId 부여된 관리자 권한 ID
     * @param passwordHash BCrypt로 인코딩된 비밀번호
     * @param otpSecret TOTP 비밀키, 등록 전에는 {@code null}
     * @param lastLoginAt 최종 로그인 완료 시각, 로그인 전에는 {@code null}
     * @param status 사용자 활성 상태
     * @param createdAt 최초 생성 시각
     * @param updatedAt 최종 수정 시각
     * @return 영속 상태로 복원된 사용자 Aggregate
     */
    public static AdminUser reconstitute(
            String id,
            String loginId,
            String name,
            String email,
            String phoneNo,
            String deptName,
            String roleId,
            String passwordHash,
            String otpSecret,
            LocalDateTime lastLoginAt,
            UserStatus status,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        return new AdminUser(
                id,
                loginId,
                name,
                email,
                phoneNo,
                deptName,
                roleId,
                passwordHash,
                otpSecret,
                lastLoginAt,
                status,
                createdAt,
                updatedAt
        );
    }

    /**
     * 현재 사용자가 로그인과 보호 API를 사용할 수 있는 상태인지 확인한다.
     *
     * @return 상태가 {@link UserStatus#ACTIVE}이면 {@code true}
     */
    public boolean isActive() {
        return status == UserStatus.ACTIVE;
    }

    /**
     * 현재 사용자가 로그인 절차를 진행할 수 있는 활성 상태인지 검증한다.
     *
     * @throws InactiveUserException 사용자가 비활성 상태인 경우
     */
    public void validateLoginAllowed() {
        if (!isActive()) {
            throw new InactiveUserException(id);
        }
    }

    /**
     * OTP 인증에 사용할 비밀키가 등록되어 있는지 확인한다.
     *
     * @return 비어 있지 않은 OTP 비밀키가 있으면 {@code true}
     */
    public boolean hasOtpSecret() {
        return otpSecret != null && !otpSecret.isBlank();
    }

    /**
     * 사용자명과 연락 정보를 새로운 프로필로 변경한다.
     *
     * @param name 변경할 사용자명
     * @param email 변경할 이메일 주소
     * @param phoneNo 변경할 연락처, 제거할 경우 {@code null}
     * @param deptName 변경할 소속 또는 부서명, 제거할 경우 {@code null}
     * @return 프로필과 수정 시각이 갱신된 사용자 Aggregate
     */
    public AdminUser changeProfile(String name, String email, String phoneNo, String deptName) {
        return new AdminUser(id, loginId, name, email, phoneNo, deptName, roleId, passwordHash,
                otpSecret, lastLoginAt, status, createdAt, LocalDateTime.now());
    }

    /**
     * 사용자에게 새로운 관리자 권한을 부여한다.
     *
     * @param roleId 새로 부여할 활성 권한 ID
     * @return 권한과 수정 시각이 갱신된 사용자 Aggregate
     */
    public AdminUser assignRole(String roleId) {
        return new AdminUser(id, loginId, name, email, phoneNo, deptName, roleId, passwordHash,
                otpSecret, lastLoginAt, status, createdAt, LocalDateTime.now());
    }

    /**
     * 사용자가 로그인과 보호 API를 사용할 수 있도록 활성화한다.
     *
     * @return 활성 상태와 수정 시각이 갱신된 사용자 Aggregate
     */
    public AdminUser activate() {
        return new AdminUser(id, loginId, name, email, phoneNo, deptName, roleId, passwordHash,
                otpSecret, lastLoginAt, UserStatus.ACTIVE, createdAt, LocalDateTime.now());
    }

    /**
     * 사용자의 로그인과 보호 API 사용을 차단하도록 비활성화한다.
     *
     * @return 비활성 상태와 수정 시각이 갱신된 사용자 Aggregate
     */
    public AdminUser deactivate() {
        return new AdminUser(id, loginId, name, email, phoneNo, deptName, roleId, passwordHash,
                otpSecret, lastLoginAt, UserStatus.INACTIVE, createdAt, LocalDateTime.now());
    }

    /**
     * 프로필과 OTP 정보는 유지하고 비밀번호 해시만 교체한다.
     *
     * @param encodedPassword Password Hash Port로 인코딩한 새 비밀번호
     * @return 새 비밀번호 해시와 갱신된 수정 시각을 가진 사용자 Aggregate
     */
    public AdminUser changePassword(String encodedPassword) {
        return new AdminUser(id, loginId, name, email, phoneNo, deptName, roleId, encodedPassword,
                otpSecret, lastLoginAt, status, createdAt, LocalDateTime.now());
    }

    /**
     * 나머지 사용자 상태는 유지하고 OTP 인증에 사용할 비밀키를 등록하거나 교체한다.
     *
     * @param secret 새 Base32 TOTP 비밀키
     * @return 새 OTP 비밀키와 갱신된 수정 시각을 가진 사용자 Aggregate
     */
    public AdminUser registerOtpSecret(String secret) {
        return new AdminUser(id, loginId, name, email, phoneNo, deptName, roleId, passwordHash,
                secret, lastLoginAt, status, createdAt, LocalDateTime.now());
    }

    /**
     * 비밀번호와 OTP 인증을 모두 마친 최종 로그인 시각을 기록한다.
     *
     * @param value 기록할 로그인 완료 시각
     * @return 최종 로그인 시각과 수정 시각이 갱신된 사용자 Aggregate
     */
    public AdminUser recordSuccessfulLogin(LocalDateTime value) {
        return new AdminUser(id, loginId, name, email, phoneNo, deptName, roleId, passwordHash,
                otpSecret, value, status, createdAt, LocalDateTime.now());
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        return object instanceof AdminUser other && id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
