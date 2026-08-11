package com.espay.admincore.adapter.out.redis.mapper;

import com.espay.admincore.adapter.out.redis.entity.PreAuthStateRedisEntity;
import com.espay.admincore.application.dto.auth.CreatePreAuthStateCommand;
import com.espay.admincore.application.dto.auth.PreAuthPurpose;
import com.espay.admincore.application.dto.auth.PreAuthState;
import org.springframework.stereotype.Component;

/**
 * Application의 사전 인증 상태와 Redis 저장 객체를 상호 변환하는 매퍼.
 */
@Component
public class PreAuthStateRedisMapper {

    /**
     * 사전 인증 생성 명령을 TTL이 포함된 신규 Redis 객체로 변환한다.
     *
     * @param command 사전 인증 토큰 ID, 사용자, 목적과 유효시간
     * @return Redis에 저장할 실패 횟수 0의 사전 인증 객체
     */
    public PreAuthStateRedisEntity toEntity(CreatePreAuthStateCommand command) {
        return PreAuthStateRedisEntity.create(
                command.jti(), command.userId(), command.purpose().name(), command.ttlSeconds());
    }

    /**
     * Redis 저장 객체를 Application에서 사용할 사전 인증 상태로 변환한다.
     *
     * @param entity Redis에서 조회한 사전 인증 객체
     * @return 인증 목적과 실패 횟수가 복원된 사전 인증 상태
     * @throws IllegalArgumentException 저장된 인증 목적을 해석할 수 없는 경우
     */
    public PreAuthState toState(PreAuthStateRedisEntity entity) {
        return new PreAuthState(entity.getJti(), entity.getUserId(),
                PreAuthPurpose.valueOf(entity.getPurpose()), entity.getFailedAttempts());
    }
}
