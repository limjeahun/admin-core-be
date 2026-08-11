package com.espay.admincore.adapter.in.web.user.request;

import com.espay.admincore.application.dto.user.UpdateUserCommand;
import jakarta.validation.constraints.Email;

/**
 * 기존 관리자 사용자의 프로필, 권한과 상태를 수정하는 HTTP 요청.
 *
 * @param name 변경할 사용자명
 * @param email 형식 검증을 적용할 변경 이메일
 * @param phoneNo 변경할 연락처
 * @param deptName 변경할 소속 또는 부서명
 * @param roleId 새로 부여할 권한 ID
 * @param status 변경할 활성 상태
 */
public record UserUpdateRequest(String name, @Email String email, String phoneNo, String deptName,
                                String roleId, String status) {
    /**
     * URL의 사용자 ID를 요청값과 결합해 수정 명령으로 변환한다.
     *
     * @param userId 수정할 사용자 ID
     * @return 사용자 수정 명령
     */
    public UpdateUserCommand toCommand(String userId) {
        return UpdateUserCommand.of(userId, name, email, phoneNo, deptName, roleId, status);
    }
}
