package com.espay.admincore.adapter.out.persistence.history.repository;

import com.espay.admincore.adapter.out.persistence.history.entity.QFileHistoryJpaEntity;
import com.espay.admincore.adapter.out.persistence.user.entity.QUserJpaEntity;
import com.espay.admincore.application.dto.history.FileHistoryQuery;
import com.espay.admincore.domain.model.file.FileHistory;
import com.espay.admincore.domain.model.menu.AdminMenuDefinition;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 파일 이력의 기간·처리 구분·성공 여부·검색어 조건과 페이지 조회를 구현한 QueryDSL 저장소.
 */
@Repository
@RequiredArgsConstructor
public class FileHistoryQueryDslRepository {
    private final JPAQueryFactory queryFactory;
    private final QFileHistoryJpaEntity history = QFileHistoryJpaEntity.fileHistoryJpaEntity;
    private final QUserJpaEntity user = QUserJpaEntity.userJpaEntity;

    /**
     * 사용자 테이블을 조인해 사용자명·로그인 ID가 포함된 현재 페이지 이력을 조회한다.
     *
     * @param query 파일 이력 검색 조건
     * @return 최신순 파일 이력 페이지
     */
    public List<FileHistory> findPage(FileHistoryQuery query) {
        return queryFactory.select(history, user.name, user.loginId)
                .from(history).join(user).on(user.id.eq(history.userId))
                .where(predicate(query)).orderBy(history.createdAt.desc(), history.id.desc())
                .offset((long) query.page() * query.size()).limit(query.size()).fetch().stream()
                .map(this::toDomain).toList();
    }

    /**
     * 목록과 동일한 조인·필터 조건으로 전체 결과 수를 계산한다.
     *
     * @param query 파일 이력 검색 조건
     * @return 전체 결과 수, 집계 결과가 없으면 0
     */
    public long count(FileHistoryQuery query) {
        Long count = queryFactory.select(history.count()).from(history)
                .join(user).on(user.id.eq(history.userId)).where(predicate(query)).fetchOne();
        return count == null ? 0 : count;
    }

    /**
     * 기간, 업로드·다운로드, 성공 여부와 선택 검색 필드를 QueryDSL 조건으로 만든다.
     *
     * @param query 파일 이력 검색 조건
     * @return where 절에 사용할 동적 조건
     */
    private BooleanBuilder predicate(FileHistoryQuery query) {
        BooleanBuilder builder = new BooleanBuilder();
        if (query.fromDate() != null) {
            builder.and(history.createdAt.goe(query.fromDate().atStartOfDay()));
        }
        if (query.toDate() != null) {
            builder.and(history.createdAt.lt(query.toDate().plusDays(1).atStartOfDay()));
        }
        if (StringUtils.hasText(query.ioType())) {
            String ioType = query.ioType().trim().toUpperCase();
            builder.and(history.ioType.eq(ioType.startsWith("U") ? "U" : "D"));
        }
        if (query.success() != null) {
            builder.and(history.resultYn.eq(query.success() ? "Y" : "N"));
        }
        if (StringUtils.hasText(query.keyword())) {
            String keyword = query.keyword().trim();
            String type = query.conditionType() == null ? "ALL" : query.conditionType().trim().toUpperCase();
            switch (type) {
                case "NAME", "USER_NAME" -> builder.and(user.name.containsIgnoreCase(keyword));
                case "LOGIN_ID", "ID" -> builder.and(user.loginId.containsIgnoreCase(keyword));
                case "IP", "CLIENT_IP" -> builder.and(history.clientIp.containsIgnoreCase(keyword));
                case "FILE_NAME" -> builder.and(history.fileName.containsIgnoreCase(keyword));
                default -> builder.and(user.name.containsIgnoreCase(keyword)
                        .or(user.loginId.containsIgnoreCase(keyword))
                        .or(history.clientIp.containsIgnoreCase(keyword))
                        .or(history.fileName.containsIgnoreCase(keyword)));
            }
        }
        return builder;
    }

    /**
     * 파일 이력 엔티티와 조인 사용자 필드를 도메인 모델로 조합하고 메뉴명을 해석한다.
     *
     * @param tuple 파일 이력 엔티티, 사용자명과 로그인 ID 조회 행
     * @return 화면 표시 정보가 채워진 파일 이력
     */
    private FileHistory toDomain(Tuple tuple) {
        var entity = tuple.get(history);
        String menuName = AdminMenuDefinition.findByCodes(List.of(entity.getMenuCode())).stream()
                .map(menu -> menu.name()).findFirst().orElse(entity.getMenuCode());
        return FileHistory.reconstitute(String.valueOf(entity.getId()), String.valueOf(entity.getUserId()),
                tuple.get(user.name), tuple.get(user.loginId), entity.getIoType(), entity.getMenuCode(), menuName,
                entity.getFileName(), entity.getFileSize(), "Y".equalsIgnoreCase(entity.getResultYn()),
                entity.getFailReason(), entity.getClientIp(), entity.getCreatedAt());
    }
}
