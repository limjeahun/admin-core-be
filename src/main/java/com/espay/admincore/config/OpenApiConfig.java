package com.espay.admincore.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger UI와 OpenAPI 문서에 공통 메타데이터 및 JWT Bearer 인증 방식을 등록하는 구성 클래스.
 */
@Configuration
public class OpenApiConfig {

    /**
     * API 제목·버전과 {@code bearerAuth} 보안 스키마를 포함한 OpenAPI 모델을 생성한다.
     * 각 API가 별도로 보안 요구 사항을 해제하지 않는 한 Swagger 요청에 Bearer JWT를 사용할 수 있다.
     *
     * @return Springdoc이 문서 생성에 사용할 OpenAPI 구성 모델
     */
    @Bean
    public OpenAPI adminCoreOpenApi() {
        String schemeName = "bearerAuth";
        return new OpenAPI()
                .info(new Info().title("Admin Core API").version("v1"))
                .addSecurityItem(new SecurityRequirement().addList(schemeName))
                .components(new Components().addSecuritySchemes(
                        schemeName,
                        new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")
                ));
    }
}
