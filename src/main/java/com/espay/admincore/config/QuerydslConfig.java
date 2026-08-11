package com.espay.admincore.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 애플리케이션의 동적 JPA 조회에서 공유할 QueryDSL 질의 팩토리를 구성한다.
 */
@Configuration
public class QuerydslConfig {

    /**
     * 현재 영속성 컨텍스트의 {@link EntityManager}를 사용하는 QueryDSL 팩토리를 생성한다.
     *
     * @param entityManager Spring이 관리하는 JPA 엔티티 매니저
     * @return 저장소에서 타입 안전한 JPQL을 조립할 질의 팩토리
     */
    @Bean
    public JPAQueryFactory jpaQueryFactory(EntityManager entityManager) {
        return new JPAQueryFactory(entityManager);
    }
}
