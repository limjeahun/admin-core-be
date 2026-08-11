package com.espay.admincore.adapter.out.security.password;

import com.espay.admincore.application.port.out.auth.PasswordHashPort;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Spring Security의 BCrypt 인코더를 이용해 비밀번호 해시 포트를 구현하는 출력 어댑터.
 */
@Component
@RequiredArgsConstructor
public class BCryptPasswordHashAdapter implements PasswordHashPort {

    private final PasswordEncoder passwordEncoder;

    /**
     * 평문 비밀번호를 BCrypt 해시로 변환한다.
     *
     * @param rawPassword 해시할 평문 비밀번호
     * @return BCrypt 비밀번호 해시
     */
    @Override
    public String encode(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    /**
     * BCrypt의 솔트와 비용 정보를 사용해 평문 비밀번호가 저장 해시와 일치하는지 확인한다.
     *
     * @param rawPassword 사용자가 입력한 평문 비밀번호
     * @param encodedPassword 저장된 BCrypt 비밀번호 해시
     * @return 비밀번호가 일치하면 {@code true}
     */
    @Override
    public boolean matches(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}
