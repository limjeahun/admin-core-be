package com.espay.admincore.adapter.out.persistence.role.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * {@code roles} 테이블의 관리자 권한 기본 정보 행을 표현하는 JPA 엔티티.
 */
@Getter
@Entity
@Table(name = "roles")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RoleJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private Long id;
    @Column(name = "role_name", nullable = false, unique = true, length = 100)
    private String name;
    @Column(name = "role_desc", length = 255)
    private String description;
    @Column(name = "use_yn", nullable = false, length = 1, columnDefinition = "char(1)")
    private String useYn;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 권한 도메인 모델을 영속화하거나 조회 결과를 복원하기 위한 전체 필드 생성자.
     *
     * @param id 권한 PK
     * @param name 권한명
     * @param description 권한 설명
     * @param useYn 사용 여부(Y/N)
     * @param createdAt 생성 시각
     * @param updatedAt 수정 시각
     */
    public RoleJpaEntity(Long id, String name, String description, String useYn,
                         LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.useYn = useYn;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
