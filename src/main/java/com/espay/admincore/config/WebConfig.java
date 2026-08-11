package com.espay.admincore.config;

import com.espay.admincore.adapter.in.web.resolver.ClientIpArgumentResolver;
import com.espay.admincore.adapter.in.web.resolver.CurrentUserIdArgumentResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Spring MVC 컨트롤러에서 인증 사용자 ID와 클라이언트 IP를 매개변수로 주입할 수 있게 구성한다.
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final CurrentUserIdArgumentResolver currentUserIdArgumentResolver;
    private final ClientIpArgumentResolver clientIpArgumentResolver;

    /**
     * 프로젝트 전용 메서드 인자 해석기를 MVC 해석기 목록에 등록한다.
     *
     * @param resolvers Spring MVC가 컨트롤러 호출 시 순서대로 사용할 인자 해석기 목록
     */
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(currentUserIdArgumentResolver);
        resolvers.add(clientIpArgumentResolver);
    }
}
