package com.espay.admincore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * 관리자 공통 백엔드 애플리케이션의 실행 진입점.
 * Spring Boot 자동 설정을 시작하고 프로젝트 내 구성 속성 클래스를 탐색하여 빈으로 등록한다.
 * 자체 포트 기반 인증을 사용하므로 기본 메모리 {@code UserDetailsService} 자동 설정은 제외한다.
 */
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
@ConfigurationPropertiesScan
public class AdminCoreApplication {

    /**
     * Spring 애플리케이션 컨텍스트와 내장 웹 서버를 시작한다.
     *
     * @param args JVM에서 전달된 애플리케이션 실행 인자
     */
    public static void main(String[] args) {
        SpringApplication.run(AdminCoreApplication.class, args);
    }
}
