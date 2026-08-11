package com.espay.admincore.domain.model.menu;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 코드에 내장된 관리자 메뉴 카탈로그.
 *
 * <p>DB에는 역할별 권한만 저장하고 메뉴의 이름, 경로, 계층과 순서는 이 열거형을 단일 기준으로 사용한다.</p>
 */
public enum AdminMenuDefinition {
    /** 사용자·권한·이력 메뉴를 묶는 최상위 운영 관리 그룹. */
    OPERATIONS("OPERATIONS", "운영 관리", null, null, 10),
    /** 관리자 사용자 조회와 편집 메뉴. */
    USERS("USERS", "사용자 관리", "/users", "OPERATIONS", 10),
    /** 관리자 권한과 메뉴 권한을 관리하는 메뉴. */
    ROLES("ROLES", "권한 관리", "/roles", "OPERATIONS", 20),
    /** 비밀번호 로그인과 OTP 인증 이력을 조회하는 메뉴. */
    LOGIN_HISTORY("LOGIN_HISTORY", "로그인 이력 조회", "/history/login", "OPERATIONS", 30),
    /** 파일 업로드와 다운로드 감사 이력을 조회하는 메뉴. */
    FILE_HISTORY("FILE_HISTORY", "파일 이력 조회", "/history/file", "OPERATIONS", 40);

    private static final Map<String, AdminMenuDefinition> BY_CODE = Arrays.stream(values())
            .collect(Collectors.toUnmodifiableMap(AdminMenuDefinition::code, Function.identity()));

    private final String code;
    private final String displayName;
    private final String path;
    private final String parentCode;
    private final int sortOrder;

    /**
     * 메뉴 카탈로그 항목을 구성한다.
     *
     * @param code 고유 메뉴 코드
     * @param displayName 화면 표시명
     * @param path 프론트엔드 경로
     * @param parentCode 상위 메뉴 코드
     * @param sortOrder 표시 순서
     */
    AdminMenuDefinition(String code, String displayName, String path, String parentCode, int sortOrder) {
        this.code = code;
        this.displayName = displayName;
        this.path = path;
        this.parentCode = parentCode;
        this.sortOrder = sortOrder;
    }

    /**
     * 권한 저장과 조회에 사용할 메뉴 코드를 반환한다.
     *
     * @return 고유 메뉴 코드
     */
    public String code() {
        return code;
    }

    /**
     * 열거형 메뉴 정의를 도메인 메뉴 값으로 변환한다.
     *
     * @return 경로와 계층 정보가 포함된 관리자 메뉴
     */
    public AdminMenu toDomain() {
        return new AdminMenu(code, displayName, path, parentCode, sortOrder);
    }

    /**
     * 대소문자와 앞뒤 공백을 무시하고 지원하는 메뉴 코드인지 확인한다.
     *
     * @param code 확인할 메뉴 코드
     * @return 카탈로그에 존재하면 {@code true}
     */
    public static boolean exists(String code) {
        return code != null && BY_CODE.containsKey(code.trim().toUpperCase(Locale.ROOT));
    }

    /**
     * 정의된 모든 메뉴를 선언 순서대로 반환한다.
     *
     * @return 전체 관리자 메뉴 목록
     */
    public static List<AdminMenu> findAll() {
        return Arrays.stream(values()).map(AdminMenuDefinition::toDomain).toList();
    }

    /**
     * 요청한 코드에 해당하는 메뉴만 선언 순서대로 반환한다.
     *
     * @param codes 조회할 메뉴 코드 모음
     * @return 일치하는 메뉴 목록, 입력이 {@code null}이면 빈 목록
     */
    public static List<AdminMenu> findByCodes(Collection<String> codes) {
        if (codes == null) {
            return List.of();
        }
        return Arrays.stream(values())
                .filter(value -> codes.stream().anyMatch(code -> value.code.equalsIgnoreCase(code)))
                .map(AdminMenuDefinition::toDomain)
                .toList();
    }
}
