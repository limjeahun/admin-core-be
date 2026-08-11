package com.espay.admincore.adapter.out.persistence.role;

import com.espay.admincore.adapter.out.persistence.role.entity.RoleMenuId;
import com.espay.admincore.adapter.out.persistence.role.entity.RoleMenuJpaEntity;
import com.espay.admincore.adapter.out.persistence.role.repository.RoleMenuJpaRepository;
import com.espay.admincore.application.port.out.menu.RoleMenuPersistencePort;
import com.espay.admincore.domain.model.role.RoleMenuPermission;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 역할별 메뉴 권한을 조회하고 삭제 후 일괄 저장 방식으로 전체 교체하는 영속성 어댑터.
 */
@Repository
@RequiredArgsConstructor
public class RoleMenuPersistenceAdapter implements RoleMenuPersistencePort {
    private final RoleMenuJpaRepository repository;

    /**
     * 권한 ID의 Y/N 엔티티 값을 boolean 도메인 권한으로 변환한다.
     *
     * @param roleId 권한 ID
     * @return 메뉴 코드 순 권한 목록
     */
    @Override
    public List<RoleMenuPermission> findByRoleId(String roleId) {
        return repository.findByIdRoleIdOrderByIdMenuCode(Long.valueOf(roleId)).stream()
                .map(entity -> new RoleMenuPermission(
                        String.valueOf(entity.getId().roleId()),
                        entity.getId().menuCode(),
                        "Y".equalsIgnoreCase(entity.getCanView()),
                        "Y".equalsIgnoreCase(entity.getCanEdit())
                )).toList();
    }

    /**
     * 기존 연결을 삭제하고 동일 시각으로 생성된 새 메뉴 권한을 일괄 저장한다.
     *
     * @param roleId 변경할 권한 ID
     * @param permissions 새로 저장할 메뉴 권한 목록
    */
    @Override
    public void replaceRoleMenus(String roleId, List<RoleMenuPermission> permissions) {
        Long id = Long.valueOf(roleId);
        repository.deleteByIdRoleId(id);
        LocalDateTime now = LocalDateTime.now();
        repository.saveAll(permissions.stream()
                .map(permission -> new RoleMenuJpaEntity(
                        new RoleMenuId(id, permission.menuCode()),
                        permission.canView() ? "Y" : "N",
                        permission.canEdit() ? "Y" : "N",
                        now,
                        now
                )).toList());
    }
}
