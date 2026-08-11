package com.espay.admincore.domain.model.role;

import com.espay.admincore.domain.exception.InactiveRoleException;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 관리자에게 부여되는 역할과 활성 상태를 표현하는 Aggregate Root.
 */
@Getter
public final class AdminRole {
    /** 영속화 이후 부여되는 권한 식별자이며 신규 Aggregate에서는 {@code null}이다. */
    private final String id;
    /** 관리자 화면과 권한 부여 과정에서 사용하는 중복 불가 권한명이다. */
    private final String name;
    /** 권한이 담당하는 업무 범위와 사용 목적을 설명하는 문구다. */
    private final String description;
    /** 로그인과 메뉴 권한 검사에서 이 권한을 사용할 수 있는지 나타낸다. */
    private final boolean active;
    /** 권한이 최초 생성된 시각이며 수정 시에도 유지된다. */
    private final LocalDateTime createdAt;
    /** 권한명, 설명 또는 활성 상태가 마지막으로 변경된 시각이다. */
    private final LocalDateTime updatedAt;

    /**
     * 신규 생성과 영속 상태 복원에 공통으로 사용하는 내부 생성자다.
     *
     * @param id 영속화된 권한 ID, 신규 권한이면 {@code null}
     * @param name 중복될 수 없는 권한명
     * @param description 권한의 업무 용도 설명
     * @param active 로그인과 권한 검사에 사용할 수 있는 활성 상태
     * @param createdAt 최초 생성 시각
     * @param updatedAt 최종 수정 시각
     */
    private AdminRole(String id, String name, String description, boolean active,
                      LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 아직 ID가 없는 신규 권한을 현재 시각으로 생성한다.
     *
     * @param name 권한명
     * @param description 권한의 업무 용도 설명
     * @param active 초기 활성 여부
     * @return 생성·수정 시각이 초기화된 신규 권한
     */
    public static AdminRole create(String name, String description, boolean active) {
        LocalDateTime now = LocalDateTime.now();
        return new AdminRole(null, name, description, active, now, now);
    }

    /**
     * 영속성에 저장된 기존 권한 상태를 Aggregate로 복원한다.
     *
     * @param id 권한 ID
     * @param name 권한명
     * @param description 권한의 업무 용도 설명
     * @param active 활성 여부
     * @param createdAt 생성 시각
     * @param updatedAt 최종 수정 시각
     * @return 복원된 권한
     */
    public static AdminRole reconstitute(String id, String name, String description, boolean active,
                                         LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new AdminRole(id, name, description, active, createdAt, updatedAt);
    }

    /**
     * 식별자와 생성 시각은 유지하면서 변경 가능한 정보를 교체한다.
     *
     * @param name 변경할 권한명
     * @param description 변경할 권한의 업무 용도 설명
     * @param active 변경할 활성 여부
     * @return 수정 시각이 갱신된 권한
     */
    public AdminRole update(String name, String description, boolean active) {
        return new AdminRole(id, name, description, active, createdAt, LocalDateTime.now());
    }

    /**
     * 현재 권한이 로그인과 보호 기능에 사용할 수 있는 활성 상태인지 검증한다.
     *
     * @throws InactiveRoleException 권한이 비활성 상태인 경우
     */
    public void validateLoginAllowed() {
        if (!active) {
            throw new InactiveRoleException(id);
        }
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        return object instanceof AdminRole other && id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
