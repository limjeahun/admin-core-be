package com.espay.admincore.adapter.out.persistence.user.mapper;

import com.espay.admincore.adapter.out.persistence.user.entity.UserJpaEntity;
import com.espay.admincore.domain.model.user.AdminUser;
import com.espay.admincore.domain.model.user.UserStatus;
import org.springframework.stereotype.Component;

/**
 * 관리자 사용자 도메인 모델과 {@code users} JPA 엔티티를 상호 변환하는 매퍼.
 */
@Component
public class UserPersistenceMapper {

    /**
     * 문자열 식별자와 도메인 상태를 Long 식별자와 Y/N DB 값으로 변환한다.
     *
     * @param user 변환할 사용자
     * @return 저장 가능한 사용자 엔티티
     */
    public UserJpaEntity toEntity(AdminUser user) {
        return new UserJpaEntity(id(user.getId()), user.getLoginId(), user.getName(), user.getEmail(), user.getPhoneNo(),
                user.getDeptName(), id(user.getRoleId()), user.getPasswordHash(), user.getStatus().toYn(), user.getOtpSecret(),
                user.getLastLoginAt(), user.getCreatedAt(), user.getUpdatedAt());
    }

    /**
     * 사용자 엔티티의 식별자와 Y/N 상태를 도메인 타입으로 복원한다.
     *
     * @param entity 조회한 사용자 엔티티
     * @return 사용자 도메인 모델
     */
    public AdminUser toDomain(UserJpaEntity entity) {
        return AdminUser.reconstitute(String.valueOf(entity.getId()), entity.getLoginId(), entity.getName(), entity.getEmail(),
                entity.getPhoneNo(), entity.getDeptName(), String.valueOf(entity.getRoleId()), entity.getPasswordHash(),
                entity.getOtpSecret(), entity.getLastLoginAt(), UserStatus.from(entity.getStatus()),
                entity.getCreatedAt(), entity.getUpdatedAt());
    }

    /**
     * nullable 문자열 식별자를 nullable Long으로 변환한다.
     *
     * @param value 변환할 ID 문자열
     * @return Long ID 또는 {@code null}
     */
    private Long id(String value) {
        return value == null ? null : Long.valueOf(value);
    }
}
