package com.espay.admincore.application.service.history;

import com.espay.admincore.application.dto.history.*;
import com.espay.admincore.application.port.in.history.FileHistoryQueryUseCase;
import com.espay.admincore.application.port.in.history.LoginHistoryQueryUseCase;
import com.espay.admincore.application.port.out.history.FileHistoryPersistencePort;
import com.espay.admincore.application.port.out.history.LoginHistoryPersistencePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 로그인·OTP 인증 이력과 파일 처리 이력의 페이지 조회를 제공하는 서비스.
 */
@Service
@RequiredArgsConstructor
public class HistoryService implements LoginHistoryQueryUseCase, FileHistoryQueryUseCase {
    private final LoginHistoryPersistencePort   loginHistoryPort;
    private final FileHistoryPersistencePort    fileHistoryPort;

    /**
     * 로그인 이력과 전체 건수를 조회해 애플리케이션 페이지 결과로 변환한다.
     *
     * @param query 로그인 이력 검색 조건
     * @return 로그인 이력 페이지
     */
    @Override
    @Transactional(readOnly = true)
    public LoginHistoryListResult getLoginHistory(LoginHistoryQuery query) {
        var items = loginHistoryPort.findPage(query).stream()
                .map(LoginHistoryResult::from)
                .toList();
        return new LoginHistoryListResult(
                items,
                loginHistoryPort.count(query),
                query.page(),
                query.size()
        );
    }

    /**
     * 파일 이력과 전체 건수를 조회해 애플리케이션 페이지 결과로 변환한다.
     *
     * @param query 파일 이력 검색 조건
     * @return 파일 이력 페이지
     */
    @Override
    @Transactional(readOnly = true)
    public FileHistoryListResult getFileHistory(FileHistoryQuery query) {
        var items = fileHistoryPort.findPage(query).stream()
                .map(FileHistoryResult::from)
                .toList();
        return new FileHistoryListResult(
                items,
                fileHistoryPort.count(query),
                query.page(),
                query.size()
        );
    }
}
