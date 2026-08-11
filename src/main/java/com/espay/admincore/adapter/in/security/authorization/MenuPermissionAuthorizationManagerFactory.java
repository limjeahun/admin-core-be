package com.espay.admincore.adapter.in.security.authorization;

import com.espay.admincore.application.port.in.menu.CheckMenuPermissionUseCase;
import com.espay.admincore.domain.model.menu.MenuPermissionAction;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Security 설정에서 사용할 메뉴 조회·편집 인가 관리자를 생성하는 팩토리.
 */
@Component
@RequiredArgsConstructor
public class MenuPermissionAuthorizationManagerFactory {
    private final CheckMenuPermissionUseCase useCase;

    /**
     * 후보 메뉴 중 하나의 조회 권한을 요구하는 인가 관리자를 생성한다.
     *
     * @param menuCodes 허용 후보 메뉴 코드
     * @return 조회 권한 인가 관리자
     */
    public AuthorizationManager<RequestAuthorizationContext> canView(String... menuCodes) {
        return create(MenuPermissionAction.VIEW, menuCodes);
    }

    /**
     * 후보 메뉴 중 하나의 편집 권한을 요구하는 인가 관리자를 생성한다.
     *
     * @param menuCodes 허용 후보 메뉴 코드
     * @return 편집 권한 인가 관리자
     */
    public AuthorizationManager<RequestAuthorizationContext> canEdit(String... menuCodes) {
        return create(MenuPermissionAction.EDIT, menuCodes);
    }

    /**
     * 메뉴 코드를 대문자로 정규화해 지정 작업의 인가 관리자를 조립한다.
     *
     * @param action 검사할 조회 또는 편집 작업
     * @param menuCodes 허용 후보 메뉴 코드
     * @return 요청 단위 인가 관리자
     */
    private AuthorizationManager<RequestAuthorizationContext> create(MenuPermissionAction action, String[] menuCodes) {
        Set<String> normalized = Arrays.stream(menuCodes)
                .map(String::toUpperCase).collect(Collectors.toSet());
        return new MenuPermissionAuthorizationManager(useCase, normalized, action);
    }
}
