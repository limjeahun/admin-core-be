package com.espay.admincore.adapter.in.security.authentication;

import com.espay.admincore.common.api.ApiFailure;
import com.espay.admincore.common.api.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 인증되지 않은 요청이 보호 자원에 접근할 때 공통 JSON 401 응답을 작성하는 입력 어댑터.
 */
@Component
@RequiredArgsConstructor
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper objectMapper;

    /**
     * 인증 실패 응답의 상태와 콘텐츠 정보를 지정하고 공통 오류 객체를 JSON으로 직렬화한다.
     *
     * @param request 인증이 필요한 HTTP 요청
     * @param response 오류를 기록할 HTTP 응답
     * @param authException Spring Security가 전달한 인증 예외
     * @throws IOException 응답 본문 직렬화 또는 출력에 실패한 경우
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(),
                ApiResponse.error(new ApiFailure(401, "UNAUTHORIZED", "인증이 필요합니다.")));
    }
}
