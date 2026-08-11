package com.espay.admincore.common.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * 모든 HTTP 요청에 추적 ID를 부여하고 로그 MDC와 응답 헤더에 전파하는 최우선 순위 필터.
 * 클라이언트가 {@code X-Request-Id}를 보내면 이를 유지하고, 없으면 새로운 UUID를 생성한다.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestCorrelationFilter extends OncePerRequestFilter {

    public static final String HEADER = "X-Request-Id";
    public static final String MDC_KEY = "requestId";

    /**
     * 요청 추적 ID를 MDC에 설정한 상태로 후속 필터와 컨트롤러를 실행하고 응답에도 같은 값을 기록한다.
     * 요청 처리가 끝나면 스레드 재사용 시 이전 ID가 섞이지 않도록 {@code finally}에서 MDC 값을 제거한다.
     *
     * @param request 현재 HTTP 요청
     * @param response 현재 HTTP 응답
     * @param filterChain 다음 필터와 요청 처리기로 이어지는 체인
     * @throws ServletException 후속 서블릿 처리에 실패한 경우
     * @throws IOException 요청 또는 응답 스트림 처리에 실패한 경우
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String requestId = request.getHeader(HEADER);
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        }
        MDC.put(MDC_KEY, requestId);
        response.setHeader(HEADER, requestId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_KEY);
        }
    }
}
