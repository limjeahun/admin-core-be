package com.espay.admincore.adapter.out.persistence.history;

import com.espay.admincore.domain.model.file.FileHistory;
import com.espay.admincore.domain.model.history.LoginHistory;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 감사 이력 Persistence Adapter의 저장 메서드가 독립 트랜잭션 경계를 명시하는지 검증한다.
 */
class HistoryPersistenceTransactionTest {

    /**
     * 로그인 이력과 파일 이력 저장이 모두 {@code REQUIRES_NEW}로 선언되었는지 확인한다.
     * 이 규칙은 호출 서비스의 성공·실패 및 읽기 전용 트랜잭션과 무관하게 감사 이력을 저장하도록 보호한다.
     *
     * @throws NoSuchMethodException 검사할 공개 저장 메서드를 찾지 못한 경우
     */
    @Test
    void historySaveMethodsUseRequiresNewTransactions() throws NoSuchMethodException {
        assertRequiresNew(LoginHistoryPersistenceAdapter.class.getMethod("save", LoginHistory.class));
        assertRequiresNew(FileHistoryPersistenceAdapter.class.getMethod("save", FileHistory.class));
    }

    /**
     * 지정한 메서드의 Spring 트랜잭션 선언과 전파 속성을 검사한다.
     *
     * @param method 트랜잭션 선언을 확인할 Persistence Adapter 메서드
     */
    private void assertRequiresNew(Method method) {
        Transactional transactional = method.getAnnotation(Transactional.class);
        assertThat(transactional).as(method + "의 @Transactional 선언").isNotNull();
        assertThat(transactional.propagation()).isEqualTo(Propagation.REQUIRES_NEW);
    }
}
