package com.espay.admincore.adapter.out.persistence.role.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * {@code permissions} 테이블의 역할별 메뉴 조회·편집 권한 행을 표현하는 JPA 엔티티.
 */
@Getter
@Entity
@Table(name = "permissions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RoleMenuJpaEntity {
    @EmbeddedId
    private RoleMenuId id;
    @Column(name = "can_view", nullable = false, length = 1, columnDefinition = "char(1)")
    private String canView;
    @Column(name = "can_edit", nullable = false, length = 1, columnDefinition = "char(1)")
    private String canEdit;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 도메인 메뉴 권한을 영속화하기 위한 전체 필드 생성자.
     *
     * @param id 권한 ID와 메뉴 코드 복합키
     * @param canView 조회 허용 여부(Y/N)
     * @param canEdit 편집 허용 여부(Y/N)
     * @param createdAt 생성 시각
     * @param updatedAt 수정 시각
     */
    public RoleMenuJpaEntity(RoleMenuId id, String canView, String canEdit,
                             LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.canView = canView;
        this.canEdit = canEdit;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
