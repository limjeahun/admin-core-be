package com.espay.admincore.adapter.out.redis;

import com.espay.admincore.adapter.out.redis.mapper.PreAuthStateRedisMapper;
import com.espay.admincore.adapter.out.redis.repository.PreAuthStateRedisRepository;
import com.espay.admincore.application.dto.auth.ConsumePreAuthStateCommand;
import com.espay.admincore.application.dto.auth.CreatePreAuthStateCommand;
import com.espay.admincore.application.dto.auth.PreAuthState;
import com.espay.admincore.application.port.out.auth.PreAuthStatePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data Redis Repository를 사전 인증 상태 Port에 연결하는 출력 어댑터.
 */
@Repository
@RequiredArgsConstructor
public class RedisPreAuthStateAdapter implements PreAuthStatePort {
    private final PreAuthStateRedisRepository repository;
    private final PreAuthStateRedisMapper mapper;

    /**
     * 생성 명령을 TTL이 포함된 Redis Hash 객체로 변환해 저장한다.
     *
     * @param command 토큰 ID, 사용자, 목적과 유효시간을 묶은 생성 명령
     */
    @Override
    public void create(CreatePreAuthStateCommand command) {
        repository.save(mapper.toEntity(command));
    }

    /**
     * Redis Repository에서 객체를 조회해 Application의 사전 인증 상태로 변환한다.
     *
     * @param jti 사전 인증 토큰 ID
     * @return 상태가 없거나 저장 형식을 해석할 수 없으면 빈 값
     */
    @Override
    public Optional<PreAuthState> find(String jti) {
        try {
            return repository.findById(jti).map(mapper::toState);
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }

    /**
     * Lua Script로 실패 횟수를 원자적으로 증가시키고 한도 도달 시 키를 삭제한다.
     *
     * @param jti 사전 인증 토큰 ID
     * @param maxAttempts 허용할 최대 OTP 실패 횟수
     * @return 증가된 횟수, 키가 없으면 -1
     */
    @Override
    public int recordFailure(String jti, int maxAttempts) {
        return repository.incrementFailureAndDeleteAtLimit(jti, maxAttempts);
    }

    /**
     * Lua Script로 사용자와 목적을 확인한 뒤 일치하는 키를 원자적으로 삭제한다.
     *
     * @param command 토큰 ID, 기대 사용자와 인증 목적을 묶은 소비 명령
     * @return 검증과 삭제가 성공하면 {@code true}
     */
    @Override
    public boolean consume(ConsumePreAuthStateCommand command) {
        return repository.consumeIfMatches(
                command.jti(), command.userId(), command.purpose().name());
    }
}
