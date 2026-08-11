package com.espay.admincore.adapter.in.security.authentication;

import com.espay.admincore.application.port.in.auth.AccessTokenAuthenticationUseCase;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 매 HTTP 요청의 Bearer Access Token 을 검증해 Spring Security 인증 컨텍스트를 구성하는 필터.
 *
 * <p>유효한 토큰이라도 DB의 현재 사용자 또는 권한이 비활성이면 인증 정보를 설정하지 않는다.</p>
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final AccessTokenAuthenticationUseCase accessTokenAuthenticationUseCase;

    /**
     * Authorization 헤더의 ACCESS JWT 를 검증하고 현재 사용자 정보로 인증 객체를 생성한다.
     *
     * <p>토큰 또는 사용자 조회 오류는 요청을 즉시 실패시키지 않고 인증 컨텍스트를 비운 채 다음 필터로 전달한다.</p>
     *
     * @param request 현재 HTTP 요청
     * @param response 현재 HTTP 응답
     * @param filterChain 다음 필터를 호출할 체인
     * @throws ServletException 다음 필터 처리 중 Servlet 오류가 발생한 경우
     * @throws IOException 요청·응답 처리 중 I/O 오류가 발생한 경우
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        // 1. 요청 헤더에서 Access Token을 확인한다.
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                // 2. 토큰과 현재 사용자·역할 상태를 검증한다.
                var account = accessTokenAuthenticationUseCase.authenticate(token);
                // 3. 검증된 계정을 Spring Security 인증 정보로 변환한다.
                var principal = AdminPrincipal.authenticated(
                        new AdminPrincipalData(
                                account.userId(),
                                account.loginId(),
                                account.name(),
                                account.roleId()
                        )
                );
                var authentication = UsernamePasswordAuthenticationToken.authenticated(
                        principal, token, principal.getAuthorities()
                );
                // 4. 현재 요청에서 인증된 사용자로 사용할 수 있도록 저장한다.
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (RuntimeException ignored) {
                // 인증에 실패하면 미인증 상태로 처리하고 URL 접근 규칙에서 401 여부를 판단한다.
                SecurityContextHolder.clearContext();
            }
        }
        // 5. 인증 결과를 유지한 채 다음 보안 필터로 요청을 전달한다.
        filterChain.doFilter(request, response);
    }
}
