package com.espay.admincore.adapter.out.persistence.role.mapper;

import com.espay.admincore.adapter.out.persistence.role.entity.RoleJpaEntity;
import com.espay.admincore.domain.model.role.AdminRole;
import org.springframework.stereotype.Component;

/**
 * 관리자 권한 도메인 모델과 {@code roles} JPA 엔티티를 상호 변환하는 매퍼.
 */
@Component
public class RolePersistenceMapper {
    /**
     * 문자열 ID와 boolean 활성 상태를 Long과 Y/N DB 타입으로 변환한다.
     *
     * @param role 변환할 권한
     * @return 저장 가능한 권한 엔티티
     */
    public RoleJpaEntity toEntity(AdminRole role) {
        return new RoleJpaEntity(role.getId() == null ? null : Long.valueOf(role.getId()), role.getName(), role.getDescription(),
                role.isActive() ? "Y" : "N", role.getCreatedAt(), role.getUpdatedAt());
    }

    /**
     * 권한 엔티티의 ID와 Y/N 상태를 도메인 타입으로 변환한다.
     *
     * @param entity 조회한 권한 엔티티
     * @return 권한 도메인 모델
     */
    public AdminRole toDomain(RoleJpaEntity entity) {
        return AdminRole.reconstitute(String.valueOf(entity.getId()), entity.getName(), entity.getDescription(),
                "Y".equalsIgnoreCase(entity.getUseYn()), entity.getCreatedAt(), entity.getUpdatedAt());
    }
}
