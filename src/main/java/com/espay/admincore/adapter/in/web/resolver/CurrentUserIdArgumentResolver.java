package com.espay.admincore.adapter.in.web.resolver;

import com.espay.admincore.adapter.in.security.authentication.AdminPrincipal;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * Spring SecurityContext의 {@link AdminPrincipal}에서 사용자 ID를 꺼내 {@link CurrentUserId} 매개변수에 주입한다.
 */
@Component
public class CurrentUserIdArgumentResolver implements HandlerMethodArgumentResolver {

    /**
     * 문자열 타입이며 {@link CurrentUserId}가 지정된 매개변수만 처리 대상으로 판별한다.
     *
     * @param parameter 호출할 컨트롤러 메서드의 매개변수 정보
     * @return 현재 해석기가 값을 제공할 수 있으면 {@code true}
     */
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUserId.class)
                && parameter.getParameterType() == String.class;
    }

    /**
     * 현재 인증 객체의 principal이 {@link AdminPrincipal}이면 내부 사용자 ID를 반환한다.
     *
     * @param parameter 대상 컨트롤러 매개변수 정보
     * @param container 현재 MVC 모델과 뷰 컨테이너
     * @param request 현재 네이티브 웹 요청
     * @param binderFactory 요청 값 바인딩 팩토리
     * @return 인증 사용자 ID, 인증 정보가 없거나 principal 타입이 다르면 {@code null}
     */
    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer container,
                                  NativeWebRequest request, WebDataBinderFactory binderFactory) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AdminPrincipal principal)) {
            return null;
        }
        return principal.getId();
    }
}
