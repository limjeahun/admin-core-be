package com.espay.admincore.adapter.out.persistence.history.mapper;

import com.espay.admincore.adapter.out.persistence.history.entity.LoginHistoryJpaEntity;
import com.espay.admincore.domain.model.history.LoginHistory;
import org.springframework.stereotype.Component;

/**
 * 로그인 이력 도메인 모델과 {@code login_logs} JPA 엔티티를 상호 변환하는 매퍼.
 */
@Component
public class LoginHistoryPersistenceMapper {
    /**
     * nullable 문자열 사용자 ID와 boolean 성공 여부를 DB 타입으로 변환한다.
     *
     * @param history 변환할 도메인 이력
     * @return 저장 가능한 JPA 엔티티
     */
    public LoginHistoryJpaEntity toEntity(LoginHistory history) {
        return new LoginHistoryJpaEntity(history.getId() == null ? null : Long.valueOf(history.getId()),
                history.getUserId() == null ? null : Long.valueOf(history.getUserId()), history.getAuthStep(),
                history.isSuccess() ? "Y" : "N", history.getLoginReason(), history.getFailReason(), history.getInputId(),
                history.getClientIp(), history.getUserAgent(), history.getCreatedAt());
    }

    /**
     * JPA 엔티티를 기본 도메인 이력으로 복원한다.
     *
     * <p>사용자명과 로그인 ID는 엔티티에 없으므로 목록 동적 조회에서 별도로 조합한다.</p>
     *
     * @param entity 조회한 로그인 이력 엔티티
     * @return 도메인 로그인 이력
     */
    public LoginHistory toDomain(LoginHistoryJpaEntity entity) {
        return LoginHistory.reconstitute(String.valueOf(entity.getId()),
                entity.getUserId() == null ? null : String.valueOf(entity.getUserId()), null, null,
                entity.getAuthStep(), "Y".equalsIgnoreCase(entity.getResultYn()), entity.getLoginReason(),
                entity.getFailReason(), entity.getInputId(), entity.getClientIp(), entity.getUserAgent(),
                entity.getCreatedAt());
    }
}
