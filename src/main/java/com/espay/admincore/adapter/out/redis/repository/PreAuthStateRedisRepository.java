package com.espay.admincore.adapter.out.redis.repository;

import com.espay.admincore.adapter.out.redis.entity.PreAuthStateRedisEntity;
import org.springframework.data.repository.ListCrudRepository;

/**
 * 사전 인증 Redis 객체의 기본 CRUD와 원자적 상태 변경을 제공하는 Repository.
 */
public interface PreAuthStateRedisRepository
        extends ListCrudRepository<PreAuthStateRedisEntity, String>, PreAuthStateRedisAtomicOperations {
}
