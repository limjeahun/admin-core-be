package com.espay.admincore.adapter.in.security.authentication;

import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * JWT 인증이 완료된 관리자 계정을 Spring Security의 {@link UserDetails}로 변환한 입력 어댑터 모델.
 * 애플리케이션과 도메인 계층에는 노출하지 않고 SecurityContext 내부에서만 사용한다.
 */
@Getter
@Builder
public class AdminPrincipal implements UserDetails {

    private final String id;
    private final String username;
    private final String password;
    private final String name;
    private final String roleId;
    private final boolean enabled;
    @Builder.Default
    private final Collection<? extends GrantedAuthority> authorities = List.of();

    /**
     * 검증된 애플리케이션 인증 결과를 SecurityContext에 저장할 principal로 변환한다.
     *
     * @param data 사용자, 로그인 ID, 이름과 활성 권한을 묶은 변환 입력값
     * @return 비밀번호 없이 활성 상태와 Spring 권한을 가진 principal
     */
    public static AdminPrincipal authenticated(AdminPrincipalData data) {
        return AdminPrincipal.builder()
                .id(data.userId())
                .username(data.loginId())
                .password("")
                .name(data.name())
                .roleId(data.roleId())
                .enabled(true)
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + data.roleId())))
                .build();
    }

    /**
     * 별도의 계정 만료 정책을 사용하지 않으므로 항상 만료되지 않은 것으로 판단한다.
     *
     * @return 항상 {@code true}
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * 애플리케이션 유스케이스에서 활성 상태를 검증했으므로 principal의 활성 값을 반환한다.
     *
     * @return 현재 요청에서 인증 가능한 principal이면 {@code true}
     */
    @Override
    public boolean isAccountNonLocked() {
        return enabled;
    }

    /**
     * JWT 인증에서는 별도의 자격 증명 만료 정책을 사용하지 않는다.
     *
     * @return 항상 {@code true}
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
}
