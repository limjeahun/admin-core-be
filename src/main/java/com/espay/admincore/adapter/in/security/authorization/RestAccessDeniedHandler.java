package com.espay.admincore.adapter.in.security.authorization;

import com.espay.admincore.common.api.ApiFailure;
import com.espay.admincore.common.api.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 인증은 되었지만 요청 권한이 부족한 경우 공통 JSON 403 응답을 작성하는 입력 어댑터.
 */
@Component
@RequiredArgsConstructor
public class RestAccessDeniedHandler implements AccessDeniedHandler {
    private final ObjectMapper objectMapper;

    /**
     * 인가 실패 응답의 상태와 콘텐츠 정보를 지정하고 공통 오류 객체를 JSON으로 직렬화한다.
     *
     * @param request 접근이 거부된 HTTP 요청
     * @param response 오류를 기록할 HTTP 응답
     * @param accessDeniedException Spring Security가 전달한 접근 거부 예외
     * @throws IOException 응답 본문 직렬화 또는 출력에 실패한 경우
     */
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(),
                ApiResponse.error(new ApiFailure(403, "ACCESS_DENIED", "접근 권한이 없습니다.")));
    }
}
