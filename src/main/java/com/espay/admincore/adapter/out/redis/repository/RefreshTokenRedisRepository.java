package com.espay.admincore.adapter.out.redis.repository;

import com.espay.admincore.adapter.out.redis.entity.RefreshTokenRedisEntity;
import org.springframework.data.repository.ListCrudRepository;

/**
 * 사용자별 Refresh Token Redis 객체의 기본 저장·조회·삭제를 제공하는 Repository.
 */
public interface RefreshTokenRedisRepository extends ListCrudRepository<RefreshTokenRedisEntity, String>,
        RefreshTokenRedisAtomicOperations {
}
