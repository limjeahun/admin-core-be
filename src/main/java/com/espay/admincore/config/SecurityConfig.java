package com.espay.admincore.config;

import com.espay.admincore.adapter.in.security.authentication.JwtAuthenticationFilter;
import com.espay.admincore.adapter.in.security.authentication.RestAuthenticationEntryPoint;
import com.espay.admincore.adapter.in.security.authorization.MenuPermissionAuthorizationManagerFactory;
import com.espay.admincore.adapter.in.security.authorization.RestAccessDeniedHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * 관리자 API의 인증·인가 정책과 Spring Security 핵심 빈을 구성한다.
 * 세션을 생성하지 않는 JWT 방식으로 동작하며 공개 경로, 인증 전용 경로,
 * 메뉴별 조회·편집 권한을 선언하고 JWT 필터를 표준 아이디/비밀번호 인증 필터보다 먼저 실행한다.
 */
@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthenticationFilter                   jwtAuthenticationFilter;
    private final RestAuthenticationEntryPoint              authenticationEntryPoint;
    private final RestAccessDeniedHandler                   accessDeniedHandler;
    private final MenuPermissionAuthorizationManagerFactory permissions;
    private final CorsProperties                            corsProperties;

    /**
     * HTTP 요청에 적용할 무상태 보안 필터 체인과 URL별 접근 규칙을 정의한다.
     * 로그인·토큰 갱신·OTP·문서 경로는 공개하고, 나머지 업무 API는 인증 및 메뉴 권한을 검사한다.
     * 인증 실패와 인가 실패는 REST JSON 처리기로 전달하며 JWT 필터가 SecurityContext를 구성한다.
     *
     * @param http Spring Security HTTP 정책 빌더
     * @return 애플리케이션 요청에 적용할 보안 필터 체인
     * @throws Exception 보안 정책 또는 필터 체인 생성에 실패한 경우
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        CookieCsrfTokenRepository csrfTokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        CsrfTokenRequestAttributeHandler csrfTokenRequestHandler = new CsrfTokenRequestAttributeHandler();

        http.cors(Customizer.withDefaults())
                .csrf(csrf -> csrf
                        .csrfTokenRepository(csrfTokenRepository)
                        .csrfTokenRequestHandler(csrfTokenRequestHandler)
                        .requireCsrfProtectionMatcher(new OrRequestMatcher(
                                new AntPathRequestMatcher("/api/v1/auth/refresh", "POST"),
                                new AntPathRequestMatcher("/api/v1/auth/logout", "POST")
                        )))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(errors -> errors.authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/login", "/api/v1/auth/refresh", "/api/v1/auth/logout",
                                "/api/v1/auth/csrf",
                                "/api/v1/auth/otp/barcode", "/api/v1/auth/otp/verify").permitAll()
                        .requestMatchers("/actuator/**", "/api/docu", "/api/docu/**",
                                "/api/swagger-ui/**", "/api/v3/api-docs/**").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/v1/menus/me").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/v1/menus/active")
                                .access(permissions.canView("ROLES"))
                        .requestMatchers(HttpMethod.GET, "/api/v1/users", "/api/v1/users/**")
                                .access(permissions.canView("USERS"))
                        .requestMatchers("/api/v1/users", "/api/v1/users/**")
                                .access(permissions.canEdit("USERS"))
                        .requestMatchers(HttpMethod.GET, "/api/v1/roles")
                                .access(permissions.canView("ROLES", "USERS"))
                        .requestMatchers(HttpMethod.GET, "/api/v1/roles/**")
                                .access(permissions.canView("ROLES"))
                        .requestMatchers("/api/v1/roles", "/api/v1/roles/**")
                                .access(permissions.canEdit("ROLES"))
                        .requestMatchers(HttpMethod.GET, "/api/v1/history/login", "/api/v1/history/login/**")
                                .access(permissions.canView("LOGIN_HISTORY"))
                        .requestMatchers(HttpMethod.GET, "/api/v1/history/file", "/api/v1/history/file/**")
                                .access(permissions.canView("FILE_HISTORY"))
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /**
     * 브라우저 쿠키를 포함한 요청을 지정된 프론트엔드 Origin에만 허용한다.
     *
     * @return 관리자 API에 적용할 CORS 정책
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(corsProperties.allowedOrigins());
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-XSRF-TOKEN"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }

    /**
     * 로그인 비밀번호 검증과 신규 비밀번호 해싱에 사용할 BCrypt 인코더를 등록한다.
     *
     * @return BCrypt 기반 비밀번호 인코더
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
