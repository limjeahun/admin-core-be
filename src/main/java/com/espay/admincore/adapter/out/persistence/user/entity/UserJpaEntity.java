package com.espay.admincore.adapter.out.persistence.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * {@code users} 테이블의 관리자 프로필, 비밀번호 해시와 OTP 상태를 표현하는 JPA 엔티티.
 */
@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(name = "login_id", nullable = false, unique = true, length = 100)
    private String loginId;

    @Column(name = "user_name", nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "phone_no", length = 20)
    private String phoneNo;

    @Column(name = "dept_name", length = 50)
    private String deptName;

    @Column(name = "role_id", nullable = false)
    private Long roleId;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "user_status", nullable = false, length = 1, columnDefinition = "char(1)")
    private String status;

    @Column(name = "otp_secret", length = 100)
    private String otpSecret;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 사용자 도메인 모델을 영속화하거나 조회 결과를 복원하기 위한 전체 필드 생성자.
     *
     * @param id 사용자 PK
     * @param loginId 로그인 ID
     * @param name 사용자명
     * @param email 이메일
     * @param phoneNo 연락처
     * @param deptName 소속 또는 부서
     * @param roleId 권한 FK
     * @param passwordHash BCrypt 비밀번호 해시
     * @param status 활성 상태(Y/N)
     * @param otpSecret Base32 TOTP 비밀키
     * @param lastLoginAt 최종 로그인 시각
     * @param createdAt 생성 시각
     * @param updatedAt 수정 시각
     */
    public UserJpaEntity(Long id, String loginId, String name, String email, String phoneNo, String deptName,
                         Long roleId, String passwordHash, String status, String otpSecret,
                         LocalDateTime lastLoginAt, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.loginId = loginId;
        this.name = name;
        this.email = email;
        this.phoneNo = phoneNo;
        this.deptName = deptName;
        this.roleId = roleId;
        this.passwordHash = passwordHash;
        this.status = status;
        this.otpSecret = otpSecret;
        this.lastLoginAt = lastLoginAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
