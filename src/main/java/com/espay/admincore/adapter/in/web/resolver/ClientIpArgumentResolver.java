package com.espay.admincore.adapter.in.web.resolver;

import com.espay.admincore.config.TrustedProxyProperties;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * {@link ClientIp}가 지정된 컨트롤러 매개변수에 요청자의 IP 주소를 주입한다.
 * 신뢰 프록시 모드에서만 전달 헤더를 읽고 그 외에는 연결의 원격 주소를 사용한다.
 */
@Component
@RequiredArgsConstructor
public class ClientIpArgumentResolver implements HandlerMethodArgumentResolver {

    private final TrustedProxyProperties trustedProxyProperties;

    /**
     * 문자열 타입이며 {@link ClientIp}가 지정된 매개변수만 처리 대상으로 판별한다.
     *
     * @param parameter 호출할 컨트롤러 메서드의 매개변수 정보
     * @return 이 해석기가 값을 제공할 수 있으면 {@code true}
     */
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(ClientIp.class)
                && parameter.getParameterType() == String.class;
    }

    /**
     * 신뢰 프록시가 활성화되면 전달 헤더를 확인하고, 유효한 값이 없으면 연결의 원격 주소를 반환한다.
     *
     * @param parameter 대상 컨트롤러 매개변수 정보
     * @param container 현재 MVC 모델과 뷰 컨테이너
     * @param request 현재 네이티브 웹 요청
     * @param binderFactory 요청 값 바인딩 팩토리
     * @return 해석된 클라이언트 IP, 서블릿 요청을 얻지 못하면 {@code null}
     */
    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer container,
                                  NativeWebRequest request, WebDataBinderFactory binderFactory) {
        HttpServletRequest servletRequest = request.getNativeRequest(HttpServletRequest.class);
        if (servletRequest == null) {
            return null;
        }
        if (trustedProxyProperties.enabled()) {
            String forwardedFor = servletRequest.getHeader("X-Forwarded-For");
            if (forwardedFor != null && !forwardedFor.isBlank()) {
                return forwardedFor.split(",")[0].trim();
            }
            String realIp = servletRequest.getHeader("X-Real-IP");
            if (realIp != null && !realIp.isBlank()) {
                return realIp.trim();
            }
        }
        return servletRequest.getRemoteAddr();
    }
}
