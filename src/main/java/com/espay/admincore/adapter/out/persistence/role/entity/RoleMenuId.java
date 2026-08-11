package com.espay.admincore.adapter.out.persistence.role.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;

/**
 * {@code permissions} 테이블에서 권한 ID와 메뉴 코드로 구성된 복합 PK.
 *
 * @param roleId 관리자 권한 ID
 * @param menuCode 메뉴 코드
 */
@Embeddable
public record RoleMenuId(
        @Column(name = "role_id") Long roleId,
        @Column(name = "menu_code", length = 30) String menuCode
) implements Serializable {
    /**
     * JPA가 복합키를 복원할 때 사용할 빈 생성자를 제공한다.
     */
    public RoleMenuId() {
        this(null, null);
    }
}
