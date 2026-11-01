package com.espay.admincore;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * 외장 Tomcat이 WAR를 배포할 때 관리자 백엔드 애플리케이션을 초기화한다.
 */
public class AdminCoreServletInitializer extends SpringBootServletInitializer {

    /**
     * 외장 서블릿 컨테이너에서 사용할 Spring Boot 애플리케이션 구성을 지정한다.
     *
     * @param application 서블릿 컨테이너가 제공한 애플리케이션 빌더
     * @return 관리자 백엔드 구성이 등록된 애플리케이션 빌더
     */
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(AdminCoreApplication.class);
    }
}
