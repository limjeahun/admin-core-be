package com.espay.admincore.application.dto.menu;

/**
 * 관리자 화면에 표시할 메뉴와 현재 권한의 연결 상태.
 *
 * @param menuCode 메뉴 고유 코드
 * @param name 화면 표시명
 * @param path 프론트엔드 라우팅 경로
 * @param parentMenuCode 상위 메뉴 코드
 * @param sortOrder 동일 계층 내 표시 순서
 * @param assigned 현재 권한에 메뉴가 연결되어 있는지 여부
 * @param canView 조회 권한 여부
 * @param canEdit 편집 권한 여부
 */
public record MenuResult(
        String menuCode,
        String name,
        String path,
        String parentMenuCode,
        int sortOrder,
        boolean assigned,
        boolean canView,
        boolean canEdit
) {
    /**
     * 메뉴 정의와 권한 상태를 하나의 조회 결과로 조합한다.
     *
     * @param source 메뉴 정의와 권한 상태를 묶은 변환 원본
     * @return 화면에 반환할 메뉴 결과
     */
    public static MenuResult from(MenuResultSource source) {
        return new MenuResult(source.menu().menuCode(), source.menu().name(), source.menu().path(),
                source.menu().parentMenuCode(), source.menu().sortOrder(), source.assigned(),
                source.canView(), source.canEdit());
    }
}
