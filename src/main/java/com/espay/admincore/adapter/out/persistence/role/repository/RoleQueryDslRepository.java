package com.espay.admincore.adapter.out.persistence.role.repository;

import com.espay.admincore.adapter.out.persistence.role.entity.QRoleJpaEntity;
import com.espay.admincore.adapter.out.persistence.role.entity.RoleJpaEntity;
import com.espay.admincore.application.dto.role.RoleQuery;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 권한명·설명 키워드와 페이지 조건을 처리하는 QueryDSL 저장소.
 */
@Repository
@RequiredArgsConstructor
public class RoleQueryDslRepository {
    private final JPAQueryFactory queryFactory;
    private final QRoleJpaEntity role = QRoleJpaEntity.roleJpaEntity;

    /**
     * 키워드 조건에 맞는 권한을 생성 최신순으로 페이지 조회한다.
     *
     * @param query 권한 검색 조건
     * @return 현재 페이지 권한 엔티티
     */
    public List<RoleJpaEntity> findPage(RoleQuery query) {
        return queryFactory.selectFrom(role).where(predicate(query)).orderBy(role.createdAt.desc())
                .offset((long) query.page() * query.size()).limit(query.size()).fetch();
    }

    /**
     * 동일 검색 조건에 맞는 전체 권한 수를 계산한다.
     *
     * @param query 권한 검색 조건
     * @return 전체 권한 수
     */
    public long count(RoleQuery query) {
        Long count = queryFactory.select(role.count()).from(role).where(predicate(query)).fetchOne();
        return count == null ? 0 : count;
    }

    /**
     * 권한명 또는 설명의 대소문자 무시 부분 일치 조건을 만든다.
     *
     * @param query 권한 검색 조건
     * @return QueryDSL 동적 조건
     */
    private BooleanBuilder predicate(RoleQuery query) {
        BooleanBuilder builder = new BooleanBuilder();
        if (StringUtils.hasText(query.keyword())) {
            builder.and(role.name.containsIgnoreCase(query.keyword().trim())
                    .or(role.description.containsIgnoreCase(query.keyword().trim())));
        }
        return builder;
    }
}
