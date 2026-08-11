package com.espay.admincore.architecture;

import com.espay.admincore.domain.model.file.FileHistory;
import com.espay.admincore.domain.model.history.LoginHistory;
import com.espay.admincore.domain.model.role.AdminRole;
import com.espay.admincore.domain.model.user.AdminUser;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 독립적으로 저장되는 Aggregate Root의 구현 형태와 생성 경계를 보호한다.
 */
class AggregateRootConventionTest {
    private static final List<Class<?>> AGGREGATE_ROOTS = List.of(
            AdminUser.class,
            AdminRole.class,
            LoginHistory.class,
            FileHistory.class
    );

    /**
     * 모든 Aggregate Root가 record가 아닌 상속 불가능한 class인지 확인한다.
     */
    @Test
    void aggregateRootsAreFinalClasses() {
        assertThat(AGGREGATE_ROOTS)
                .allMatch(type -> !type.isRecord())
                .allMatch(type -> Modifier.isFinal(type.getModifiers()));
    }

    /**
     * 외부에서 생성자를 우회하지 못하도록 모든 Aggregate 생성자를 private으로 제한한다.
     */
    @Test
    void aggregateRootConstructorsArePrivate() {
        assertThat(AGGREGATE_ROOTS)
                .allMatch(type -> List.of(type.getDeclaredConstructors()).stream()
                        .allMatch(constructor -> Modifier.isPrivate(constructor.getModifiers())));
    }
}
