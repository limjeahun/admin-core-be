package com.espay.admincore.adapter.out.persistence.history.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * {@code file_logs} 테이블의 파일 업로드·다운로드 감사 이력 행을 표현하는 JPA 엔티티.
 */
@Getter
@Entity
@Table(name = "file_logs")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FileHistoryJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "hist_id")
    private Long id;
    @Column(name = "user_id", nullable = false)
    private Long userId;
    @Column(name = "io_type", nullable = false, length = 1, columnDefinition = "char(1)")
    private String ioType;
    @Column(name = "menu_code", nullable = false, length = 30)
    private String menuCode;
    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;
    @Column(name = "file_size")
    private Long fileSize;
    @Column(name = "result_yn", nullable = false, length = 1, columnDefinition = "char(1)")
    private String resultYn;
    @Column(name = "fail_reason", length = 255)
    private String failReason;
    @Column(name = "client_ip", length = 64)
    private String clientIp;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * 도메인 파일 이력을 영속화하거나 조회 결과를 복원하기 위한 전체 필드 생성자.
     *
     * @param id 파일 이력 PK
     * @param userId 작업 사용자 FK
     * @param ioType 업로드(U) 또는 다운로드(D)
     * @param menuCode 작업 메뉴 코드
     * @param fileName 파일명
     * @param fileSize 파일 크기(KB)
     * @param resultYn 성공 여부(Y/N)
     * @param failReason 실패 사유
     * @param clientIp 요청 IP
     * @param createdAt 작업 시각
     */
    public FileHistoryJpaEntity(Long id, Long userId, String ioType, String menuCode, String fileName,
                                Long fileSize, String resultYn, String failReason, String clientIp,
                                LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.ioType = ioType;
        this.menuCode = menuCode;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.resultYn = resultYn;
        this.failReason = failReason;
        this.clientIp = clientIp;
        this.createdAt = createdAt;
    }
}
