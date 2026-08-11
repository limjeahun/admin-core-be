package com.espay.admincore.adapter.in.security.authorization;

import com.espay.admincore.adapter.in.security.authentication.AdminPrincipal;
import com.espay.admincore.application.dto.menu.CheckMenuPermissionQuery;
import com.espay.admincore.application.port.in.menu.CheckMenuPermissionUseCase;
import com.espay.admincore.domain.model.menu.MenuPermissionAction;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

import java.util.Set;
import java.util.function.Supplier;

/**
 * 인증 사용자에게 후보 메뉴 중 하나의 조회·편집 권한이 있는지 판단하는 요청 인가 관리자.
 */
@RequiredArgsConstructor
public class MenuPermissionAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {
    private final CheckMenuPermissionUseCase useCase;
    private final Set<String> menuCodes;
    private final MenuPermissionAction action;

    /**
     * Spring Security 인증 주체를 애플리케이션 권한 질의로 변환해 접근 결정을 반환한다.
     *
     * @param authenticationSupplier 현재 요청의 인증 정보 공급자
     * @param context HTTP 요청 인가 컨텍스트
     * @return 관리자 주체가 아니면 거부, 권한 유스케이스 결과에 따른 허용·거부 결정
     */
    @Override
    public AuthorizationDecision check(Supplier<Authentication> authenticationSupplier,
                                       RequestAuthorizationContext context) {
        Authentication authentication = authenticationSupplier.get();
        if (authentication == null || !(authentication.getPrincipal() instanceof AdminPrincipal principal)) {
            return new AuthorizationDecision(false);
        }
        return new AuthorizationDecision(useCase.isAllowed(
                CheckMenuPermissionQuery.of(principal.getId(), menuCodes, action)));
    }
}
