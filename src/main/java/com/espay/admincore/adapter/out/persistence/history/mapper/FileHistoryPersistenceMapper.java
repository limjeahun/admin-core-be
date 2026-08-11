package com.espay.admincore.adapter.out.persistence.history.mapper;

import com.espay.admincore.adapter.out.persistence.history.entity.FileHistoryJpaEntity;
import com.espay.admincore.domain.model.file.FileHistory;
import org.springframework.stereotype.Component;

/**
 * 파일 이력 도메인 모델과 {@code file_logs} JPA 엔티티를 상호 변환하는 매퍼.
 */
@Component
public class FileHistoryPersistenceMapper {
    /**
     * 문자열 ID와 boolean 성공 여부를 DB 타입인 Long과 Y/N으로 변환한다.
     *
     * @param history 변환할 도메인 이력
     * @return 저장 가능한 JPA 엔티티
     */
    public FileHistoryJpaEntity toEntity(FileHistory history) {
        return new FileHistoryJpaEntity(history.getId() == null ? null : Long.valueOf(history.getId()),
                Long.valueOf(history.getUserId()), history.getIoType(), history.getMenuCode(), history.getFileName(),
                history.getFileSize(), history.isSuccess() ? "Y" : "N", history.getFailReason(), history.getClientIp(),
                history.getCreatedAt());
    }

    /**
     * JPA 엔티티를 기본 도메인 이력으로 복원한다.
     *
     * <p>사용자명·로그인 ID·메뉴명은 이 엔티티에 없으므로 동적 조회에서 별도로 조합한다.</p>
     *
     * @param entity 조회한 파일 이력 엔티티
     * @return 도메인 파일 이력
     */
    public FileHistory toDomain(FileHistoryJpaEntity entity) {
        return FileHistory.reconstitute(String.valueOf(entity.getId()), String.valueOf(entity.getUserId()), null, null,
                entity.getIoType(), entity.getMenuCode(), null, entity.getFileName(), entity.getFileSize(),
                "Y".equalsIgnoreCase(entity.getResultYn()), entity.getFailReason(), entity.getClientIp(),
                entity.getCreatedAt());
    }
}
