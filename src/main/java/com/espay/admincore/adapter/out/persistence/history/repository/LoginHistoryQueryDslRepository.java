package com.espay.admincore.adapter.out.persistence.history.repository;

import com.espay.admincore.adapter.out.persistence.history.entity.QLoginHistoryJpaEntity;
import com.espay.admincore.adapter.out.persistence.user.entity.QUserJpaEntity;
import com.espay.admincore.application.dto.history.FindLatestLoginReasonQuery;
import com.espay.admincore.application.dto.history.LoginHistoryQuery;
import com.espay.admincore.domain.model.history.LoginHistory;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 로그인 이력의 기간·인증 단계·성공 여부·검색어 조건과 페이지 조회를 구현한 QueryDSL 저장소.
 */
@Repository
@RequiredArgsConstructor
public class LoginHistoryQueryDslRepository {
    private final JPAQueryFactory           queryFactory;
    private final QLoginHistoryJpaEntity    history = QLoginHistoryJpaEntity.loginHistoryJpaEntity;
    private final QUserJpaEntity            user    = QUserJpaEntity.userJpaEntity;

    /**
     * 실패 이력도 유지하도록 사용자를 왼쪽 조인해 현재 페이지 인증 이력을 조회한다.
     *
     * @param query 로그인 이력 검색 조건
     * @return 최신순 로그인 이력 페이지
     */
    public List<LoginHistory> findPage(LoginHistoryQuery query) {
        return queryFactory.select(history, user.name, user.loginId)
                .from(history)
                .leftJoin(user).on(user.id.eq(history.userId))
                .where(predicate(query))
                .orderBy(history.createdAt.desc(), history.id.desc())
                .offset((long) query.page() * query.size())
                .limit(query.size())
                .fetch()
                .stream().map(this::toDomain)
                .toList();
    }

    /**
     * 목록과 동일한 조인·필터 조건으로 전체 결과 수를 계산한다.
     *
     * @param query 로그인 이력 검색 조건
     * @return 전체 결과 수, 집계 결과가 없으면 0
     */
    public long count(LoginHistoryQuery query) {
        Long count = queryFactory.select(history.count()).from(history)
                .leftJoin(user).on(user.id.eq(history.userId))
                .where(predicate(query)).fetchOne();
        return count == null ? 0 : count;
    }

    /**
     * OTP 감사 이력에 연결할 가장 최근 LOGIN 단계의 접속 사유를 조회한다.
     *
     * @param query 사용자, 입력 ID와 요청 IP를 묶은 조회 조건
     * @return 최근 로그인 사유 또는 {@code null}
     */
    public String findLatestLoginReason(FindLatestLoginReasonQuery query) {
        BooleanBuilder builder = new BooleanBuilder(history.authStep.eq("LOGIN"));
        if (StringUtils.hasText(query.userId())) {
            builder.and(history.userId.eq(Long.valueOf(query.userId())));
        }
        if (StringUtils.hasText(query.inputId())) {
            builder.and(history.inputId.eq(query.inputId()));
        }
        if (StringUtils.hasText(query.clientIp())) {
            builder.and(history.clientIp.eq(query.clientIp()));
        }
        return queryFactory.select(history.loginReason).from(history).where(builder)
                .orderBy(history.createdAt.desc()).fetchFirst();
    }

    /**
     * 기간, 인증 단계, 성공 여부와 선택 검색 필드를 QueryDSL 조건으로 만든다.
     *
     * @param query 로그인 이력 검색 조건
     * @return where 절에 사용할 동적 조건
     */
    private BooleanBuilder predicate(LoginHistoryQuery query) {
        BooleanBuilder builder = new BooleanBuilder();
        if (query.fromDate() != null) {
            builder.and(history.createdAt.goe(query.fromDate().atStartOfDay()));
        }
        if (query.toDate() != null) {
            builder.and(history.createdAt.lt(query.toDate().plusDays(1).atStartOfDay()));
        }
        if (StringUtils.hasText(query.authStep())) {
            builder.and(history.authStep.eq(query.authStep().trim().toUpperCase()));
        }
        if (query.success() != null) {
            builder.and(history.resultYn.eq(query.success() ? "Y" : "N"));
        }
        if (StringUtils.hasText(query.keyword())) {
            String keyword = query.keyword().trim();
            String type = query.conditionType() == null ? "ALL" : query.conditionType().trim().toUpperCase();
            switch (type) {
                case "NAME", "USER_NAME" -> builder.and(user.name.containsIgnoreCase(keyword));
                case "LOGIN_ID", "ID" -> builder.and(user.loginId.containsIgnoreCase(keyword)
                        .or(history.inputId.containsIgnoreCase(keyword)));
                case "IP", "CLIENT_IP" -> builder.and(history.clientIp.containsIgnoreCase(keyword));
                default -> builder.and(user.name.containsIgnoreCase(keyword)
                        .or(user.loginId.containsIgnoreCase(keyword))
                        .or(history.inputId.containsIgnoreCase(keyword))
                        .or(history.clientIp.containsIgnoreCase(keyword)));
            }
        }
        return builder;
    }

    /**
     * 로그인 이력 엔티티와 선택 조인된 사용자 표시 정보를 도메인 모델로 조합한다.
     *
     * @param tuple 로그인 이력 엔티티, 사용자명과 로그인 ID 조회 행
     * @return 화면 표시 정보가 채워진 로그인 이력
     */
    private LoginHistory toDomain(Tuple tuple) {
        var entity = tuple.get(history);
        return LoginHistory.reconstitute(String.valueOf(entity.getId()),
                entity.getUserId() == null ? null : String.valueOf(entity.getUserId()),
                tuple.get(user.name), tuple.get(user.loginId), entity.getAuthStep(),
                "Y".equalsIgnoreCase(entity.getResultYn()), entity.getLoginReason(), entity.getFailReason(),
                entity.getInputId(), entity.getClientIp(), entity.getUserAgent(), entity.getCreatedAt());
    }
}
