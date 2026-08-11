package com.espay.admincore.application.port.out.auth;

import com.espay.admincore.application.dto.auth.ConsumePreAuthStateCommand;
import com.espay.admincore.application.dto.auth.CreatePreAuthStateCommand;
import com.espay.admincore.application.dto.auth.PreAuthState;

import java.util.Optional;

/**
 * 사전 인증 토큰의 서버 측 상태와 OTP 실패 횟수를 관리하는 포트.
 *
 * <p>JWT와 별도의 상태를 함께 사용해 만료, 실패 횟수 제한 및 일회성 사용을 보장한다.</p>
 */
public interface PreAuthStatePort {

    /**
     * 사전 인증 토큰에 대응하는 서버 측 상태를 생성한다.
     *
     * @param command 토큰 식별자, 사용자, 목적과 유지시간을 묶은 생성 명령
     */
    void create(CreatePreAuthStateCommand command);

    /**
     * 토큰 식별자에 해당하는 사전 인증 상태를 조회한다.
     *
     * @param jti 사전 인증 토큰 식별자
     * @return 상태가 존재하면 사전 인증 상태, 없거나 해석할 수 없으면 빈 값
     */
    Optional<PreAuthState> find(String jti);

    /**
     * OTP 실패 횟수를 원자적으로 증가시키고 최대 횟수에 도달하면 상태를 제거한다.
     *
     * @param jti 사전 인증 토큰 식별자
     * @param maxAttempts 허용할 최대 실패 횟수
     * @return 증가된 실패 횟수, 상태가 없으면 음수
     */
    int recordFailure(String jti, int maxAttempts);

    /**
     * 사용자와 인증 목적이 일치하는 사전 인증 상태를 원자적으로 확인하고 제거한다.
     *
     * @param command 토큰 식별자, 사용자와 목적을 묶은 소비 명령
     * @return 일치하는 상태를 정상적으로 소비했으면 {@code true}
     */
    boolean consume(ConsumePreAuthStateCommand command);
}
