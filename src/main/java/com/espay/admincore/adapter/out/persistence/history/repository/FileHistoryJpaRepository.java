package com.espay.admincore.adapter.out.persistence.history.repository;

import com.espay.admincore.adapter.out.persistence.history.entity.FileHistoryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 파일 이력 엔티티의 기본 CRUD를 제공하는 Spring Data JPA 저장소.
 */
public interface FileHistoryJpaRepository extends JpaRepository<FileHistoryJpaEntity, Long> {
}
