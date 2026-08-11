package com.espay.admincore.adapter.out.persistence.user.repository;

import com.espay.admincore.adapter.out.persistence.user.entity.QUserJpaEntity;
import com.espay.admincore.adapter.out.persistence.user.entity.UserJpaEntity;
import com.espay.admincore.application.dto.user.UserQuery;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 역할·상태·선택 검색 필드와 페이지 조건을 처리하는 사용자 QueryDSL 저장소.
 */
@Repository
@RequiredArgsConstructor
public class UserQueryDslRepository {

    private final JPAQueryFactory queryFactory;
    private final QUserJpaEntity user = QUserJpaEntity.userJpaEntity;

    /**
     * 동적 검색 조건에 맞는 사용자를 생성 최신순으로 페이지 조회한다.
     *
     * @param query 사용자 검색 조건
     * @return 현재 페이지 사용자 엔티티
     */
    public List<UserJpaEntity> findPage(UserQuery query) {
        return queryFactory.selectFrom(user)
                .where(predicate(query))
                .orderBy(user.createdAt.desc(), user.id.desc())
                .offset((long) query.page() * query.size())
                .limit(query.size())
                .fetch();
    }

    /**
     * 목록과 동일한 조건에 맞는 전체 사용자 수를 계산한다.
     *
     * @param query 사용자 검색 조건
     * @return 전체 사용자 수
     */
    public long count(UserQuery query) {
        Long count = queryFactory.select(user.count()).from(user).where(predicate(query)).fetchOne();
        return count == null ? 0 : count;
    }

    /**
     * 역할, 상태와 로그인 ID·이름·이메일 키워드를 QueryDSL 조건으로 만든다.
     *
     * @param query 사용자 검색 조건
     * @return where 절에 사용할 동적 조건
     */
    private BooleanBuilder predicate(UserQuery query) {
        BooleanBuilder builder = new BooleanBuilder();
        if (StringUtils.hasText(query.roleId())) {
            builder.and(user.roleId.eq(Long.valueOf(query.roleId())));
        }
        if (StringUtils.hasText(query.status())) {
            String status = "ACTIVE".equalsIgnoreCase(query.status()) || "Y".equalsIgnoreCase(query.status()) ? "Y" : "N";
            builder.and(user.status.eq(status));
        }
        if (StringUtils.hasText(query.keyword())) {
            String keyword = query.keyword().trim();
            String type = query.conditionType() == null ? "ALL" : query.conditionType().trim().toUpperCase();
            switch (type) {
                case "LOGIN_ID", "ID" -> builder.and(user.loginId.containsIgnoreCase(keyword));
                case "NAME", "USER_NAME" -> builder.and(user.name.containsIgnoreCase(keyword));
                case "EMAIL" -> builder.and(user.email.containsIgnoreCase(keyword));
                default -> builder.and(user.loginId.containsIgnoreCase(keyword)
                        .or(user.name.containsIgnoreCase(keyword))
                        .or(user.email.containsIgnoreCase(keyword)));
            }
        }
        return builder;
    }
}
