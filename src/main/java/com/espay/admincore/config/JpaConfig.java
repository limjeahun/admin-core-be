package com.espay.admincore.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA 공통 기능을 활성화하는 구성 클래스.
 * 엔티티의 생성·수정 시각을 자동으로 기록할 수 있도록 Spring Data JPA Auditing을 켠다.
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
}
