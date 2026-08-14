package com.espay.admincore.adapter.out.persistence.role;

import com.espay.admincore.adapter.out.persistence.role.entity.RoleJpaEntity;
import com.espay.admincore.adapter.out.persistence.role.mapper.RolePersistenceMapper;
import com.espay.admincore.adapter.out.persistence.role.repository.RoleJpaRepository;
import com.espay.admincore.adapter.out.persistence.role.repository.RoleQueryDslRepository;
import com.espay.admincore.domain.model.role.AdminRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RolePersistenceAdapterTest {

    @Mock
    private RoleJpaRepository repository;
    @Mock
    private RoleQueryDslRepository queryRepository;

    private RolePersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new RolePersistenceAdapter(
                repository,
                queryRepository,
                new RolePersistenceMapper()
        );
    }

    @Test
    void 문자열_권한_ID를_DB_ID로_변환해_한번에_조회한다() {
        LocalDateTime now = LocalDateTime.of(2026, 8, 1, 9, 0);
        RoleJpaEntity master = new RoleJpaEntity(1L, "MASTER", "마스터 권한", "Y", now, now);
        RoleJpaEntity operator = new RoleJpaEntity(2L, "OPERATOR", "운영 권한", "Y", now, now);
        when(repository.findAllById(List.of(1L, 2L))).thenReturn(List.of(master, operator));

        List<AdminRole> roles = adapter.findByIds(List.of("1", "2"));

        assertThat(roles)
                .extracting(AdminRole::getId, AdminRole::getName)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple("1", "MASTER"),
                        org.assertj.core.groups.Tuple.tuple("2", "OPERATOR")
                );
        verify(repository).findAllById(List.of(1L, 2L));
    }
}
