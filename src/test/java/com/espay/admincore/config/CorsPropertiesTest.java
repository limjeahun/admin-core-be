package com.espay.admincore.config;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CorsPropertiesTest {

    @Test
    void rejectsEmptyAllowedOrigins() {
        assertThatThrownBy(() -> new CorsProperties(List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("CORS 허용 Origin은 정확한 주소로 한 개 이상 설정해야 합니다.");
    }

    @Test
    void rejectsWildcardAllowedOrigin() {
        assertThatThrownBy(() -> new CorsProperties(List.of("*")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("CORS 허용 Origin은 정확한 주소로 한 개 이상 설정해야 합니다.");
    }
}
